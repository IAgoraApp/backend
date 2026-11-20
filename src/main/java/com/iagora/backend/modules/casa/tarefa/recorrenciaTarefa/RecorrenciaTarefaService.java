package com.iagora.backend.modules.casa.tarefa.recorrenciaTarefa;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.casa.tarefa.execucaoTarefa.ExecucaoTarefa;
import com.iagora.backend.modules.casa.tarefa.execucaoTarefa.ExecucaoTarefaRepository;
import com.iagora.backend.modules.casa.tarefa.tarefa.TarefaService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class RecorrenciaTarefaService {
    private final ExecucaoTarefaRepository execucaoRepository;
    private final EntityManager entityManager;
    private final RecorrenciaTarefaRepository repository;
    private final TarefaService tarefaService;

    public RecorrenciaTarefaService(ExecucaoTarefaRepository execucaoRepository, EntityManager entityManager,
                                    RecorrenciaTarefaRepository repository, TarefaService tarefaService) {
        this.execucaoRepository = execucaoRepository;
        this.entityManager = entityManager;
        this.repository = repository;
        this.tarefaService = tarefaService;
    }

    @Transactional
    public RecorrenciaTarefaResponse cadastrar(Integer tarefaId, RecorrenciaTarefaRequest request, Integer userId) {
        RecorrenciaTarefa item = new RecorrenciaTarefa();
        item.setTarefa(tarefaService.buscarDoUsuario(tarefaId, userId));
        entityManager.lock(item.getTarefa(), LockModeType.PESSIMISTIC_WRITE);
        if (repository.findByTarefa_Id(tarefaId).isPresent()) {
            throw new RegraNegocioException("A tarefa já possui recorrência", HttpStatus.CONFLICT);
        }
        aplicar(item, request, userId);
        repository.save(item);
        gerar(item, LocalDate.now());
        return resposta(item);
    }

    @Transactional
    public RecorrenciaTarefa buscarDaTarefa(Integer tarefaId, Integer userId) {
        tarefaService.buscarDoUsuario(tarefaId, userId);
        return repository.findByTarefa_Id(tarefaId).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public RecorrenciaTarefaResponse consultar(Integer tarefaId, Integer userId) {
        return resposta(buscarDaTarefa(tarefaId, userId));
    }

    @Transactional
    public void atualizar(Integer id, RecorrenciaTarefaRequest request, Integer userId) {
        var item = buscarDaTarefa(id, userId);
        entityManager.refresh(item, LockModeType.PESSIMISTIC_WRITE);
        aplicar(item, request, userId);
        limparOcorrenciasFuturas(id);
        item.setProcessadaAte(null);
        gerar(item, LocalDate.now());
    }

    @Transactional
    public void apagar(Integer id, Integer userId) {
        RecorrenciaTarefa item = buscarDaTarefa(id, userId);
        entityManager.refresh(item, LockModeType.PESSIMISTIC_WRITE);
        limparOcorrenciasFuturas(id);
        repository.delete(item);
    }

    private void aplicar(RecorrenciaTarefa item, RecorrenciaTarefaRequest request, Integer userId) {
        if (request.frequencia() != null) {
            if (request.frequencia().isBlank() || request.frequencia().strip().length() > 30) {
                throw new RegraNegocioException("Campo frequencia inválido", HttpStatus.BAD_REQUEST);
            }
            if (!List.of("diaria", "semanal", "mensal").contains(request.frequencia())) {
                throw new RegraNegocioException("Campo frequencia inválido", HttpStatus.BAD_REQUEST);
            }
            item.setFrequencia(request.frequencia().strip());
        }
        if (item.getFrequencia() == null) {
            throw new RegraNegocioException("Campo frequencia obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.intervalo() != null) {
            if (request.intervalo() < 1) {
                throw new RegraNegocioException("Campo intervalo inválido", HttpStatus.BAD_REQUEST);
            }
            item.setIntervalo(request.intervalo());
        }
        if (request.diasSemana() != null) {
            item.setDiasSemana(request.diasSemana());
        }
        if (request.diaMes() != null) {
            item.setDiaMes(request.diaMes());
        }
        if (request.inicio() != null) {
            item.setInicio(request.inicio());
        }
        if (item.getInicio() == null) {
            throw new RegraNegocioException("Campo inicio obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.fim() != null) {
            item.setFim(request.fim());
        }
        validarDias(item);
        if (item.getFim() != null && item.getFim().isBefore(item.getInicio())) {
            throw new RegraNegocioException("O término deve ser igual ou posterior ao início", HttpStatus.BAD_REQUEST);
        }
    }

    private RecorrenciaTarefaResponse resposta(RecorrenciaTarefa item) {
        return new RecorrenciaTarefaResponse(
            item.getId(),
            item.getTarefa().getId(),
            item.getFrequencia(),
            item.getIntervalo(),
            item.getDiasSemana(),
            item.getDiaMes(),
            item.getInicio(),
            item.getFim()
        );
    }

    private static final List<String> DIAS_DA_SEMANA = List.of(
        "segunda", "terca", "quarta", "quinta", "sexta", "sabado", "domingo"
    );

    private void validarDias(RecorrenciaTarefa item) {
        var semana = item.getDiasSemana();
        if ("semanal".equals(item.getFrequencia())) {
            if (semana == null || !semana.isArray() || semana.isEmpty()) {
                throw new RegraNegocioException("Informe os dias da semana", HttpStatus.BAD_REQUEST);
            }
            for (var dia : semana) {
                if (!dia.isString() || !DIAS_DA_SEMANA.contains(dia.asText())) {
                    throw new RegraNegocioException("Dia da semana inválido", HttpStatus.BAD_REQUEST);
                }
            }
        }
        var mes = item.getDiaMes();
        if ("mensal".equals(item.getFrequencia()) && mes != null && !mes.isNull()) {
            if (!mes.isArray()) {
                throw new RegraNegocioException("Informe os dias do mês em uma lista", HttpStatus.BAD_REQUEST);
            }
            for (var dia : mes) {
                if (!dia.isIntegralNumber() || dia.asLong() < 1 || dia.asLong() > 31) {
                    throw new RegraNegocioException("Dia do mês inválido", HttpStatus.BAD_REQUEST);
                }
            }
        }
    }

    @Transactional
    public void gerarOcorrencias(Integer id, LocalDate hoje) {
        var item = entityManager.find(RecorrenciaTarefa.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (item != null) {
            gerar(item, hoje);
        }
    }

    private void gerar(RecorrenciaTarefa item, LocalDate hoje) {
        LocalDate inicio = item.getProcessadaAte() == null ? hoje : item.getProcessadaAte().plusDays(1);
        if (inicio.isBefore(item.getInicio())) {
            inicio = item.getInicio();
        }
        LocalDate fim = hoje.plusDays(30);
        if (item.getFim() != null && item.getFim().isBefore(fim)) {
            fim = item.getFim();
        }
        if (inicio.isAfter(fim)) {
            return;
        }

        LocalTime horario = item.getTarefa().getPrazo() == null
            ? LocalTime.MIDNIGHT : item.getTarefa().getPrazo().toLocalTime();
        for (LocalDate data = inicio; !data.isAfter(fim); data = data.plusDays(1)) {
            var prevista = data.atTime(horario);
            if (ocorreEm(item, data) && !execucaoRepository.existsByTarefa_IdAndDataPrevista(item.getTarefa().getId(), prevista)) {
                var execucao = new ExecucaoTarefa();
                execucao.setTarefa(item.getTarefa());
                execucao.setDataPrevista(prevista);
                execucao.setGerada(true);
                execucaoRepository.save(execucao);
            }
        }
        item.setProcessadaAte(fim);
    }

    private boolean ocorreEm(RecorrenciaTarefa item, LocalDate data) {
        LocalDate inicio = item.getInicio();
        int intervalo = item.getIntervalo();
        return switch (item.getFrequencia()) {
            case "diaria" -> ChronoUnit.DAYS.between(inicio, data) % intervalo == 0;
            case "semanal" -> ChronoUnit.WEEKS.between(
                inicio.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                data.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            ) % intervalo == 0 && contemDiaDaSemana(item, data);
            case "mensal" -> ChronoUnit.MONTHS.between(YearMonth.from(inicio), YearMonth.from(data)) % intervalo == 0
                && contemDiaDoMes(item, data);
            default -> false;
        };
    }

    private boolean contemDiaDaSemana(RecorrenciaTarefa item, LocalDate data) {
        String dia = DIAS_DA_SEMANA.get(data.getDayOfWeek().getValue() - 1);
        for (var selecionado : item.getDiasSemana()) {
            if (dia.equals(selecionado.asText())) {
                return true;
            }
        }
        return false;
    }

    private boolean contemDiaDoMes(RecorrenciaTarefa item, LocalDate data) {
        var dias = item.getDiaMes();
        if (dias == null || dias.isNull() || dias.isEmpty()) {
            return data.getDayOfMonth() == Math.min(item.getInicio().getDayOfMonth(), data.lengthOfMonth());
        }
        for (var dia : dias) {
            if (dia.asInt() == data.getDayOfMonth()) {
                return true;
            }
        }
        return false;
    }

    private void limparOcorrenciasFuturas(Integer tarefaId) {
        execucaoRepository.deleteByTarefa_IdAndGeradaTrueAndStatusAndDataPrevistaGreaterThanEqual(
            tarefaId, "pendente", LocalDate.now().atStartOfDay());
        execucaoRepository.flush();
    }
}
