package br.unifor.sorveteria.estoque.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.unifor.sorveteria.estoque.domain.ItemEstoque;

public interface ItemEstoqueRepository extends JpaRepository<ItemEstoque, String> {
    List<ItemEstoque> findAllByOrderBySku();
}
