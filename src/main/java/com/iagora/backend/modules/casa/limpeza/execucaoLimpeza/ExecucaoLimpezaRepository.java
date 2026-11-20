package com.iagora.backend.modules.casa.limpeza.execucaoLimpeza;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ExecucaoLimpezaRepository extends JpaRepository<ExecucaoLimpeza, Integer> {

    List<ExecucaoLimpeza> findByLimpeza_IdOrderByRealizadaEmDescIdDesc(Integer limpezaId);

    List<ExecucaoLimpeza> findByPerfil_IdOrderByRealizadaEmDescIdDesc(Integer perfilId);

    Optional<ExecucaoLimpeza> findByIdAndLimpeza_Id(Integer id, Integer limpezaId);

    Optional<ExecucaoLimpeza> findFirstByLimpeza_IdOrderByRealizadaEmDescIdDesc(Integer limpezaId);

    void deleteByLimpeza_Id(Integer limpezaId);

    void deleteByLimpeza_Comodo_Id(Integer comodoId);

    void deleteByLimpeza_Comodo_Casa_User_Id(Integer userId);
}
