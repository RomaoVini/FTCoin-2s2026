
package br.ftcoin.dao.memory;

import br.ftcoin.dao.CarteiraDAO;
import br.ftcoin.model.Carteira;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Implementa as operações de Carteira em memória.
 */
public final class CarteiraDAOMemoria implements CarteiraDAO {

    private final Map<Integer, Carteira> carteiras =
            new LinkedHashMap<>();

    @Override
    public void incluir(Carteira carteira) {
        if (carteira == null) {
            throw new IllegalArgumentException(
                "A carteira não pode ser nula."
            );
        }

        int id = carteira.getId();

        if (carteiras.containsKey(id)) {
            throw new IllegalArgumentException(
                "Já existe uma carteira com esse identificador."
            );
        }

        carteiras.put(id, carteira);
    }

    @Override
    public Optional<Carteira> consultar(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException(
                "O identificador não pode ser nulo."
            );
        }

        return Optional.ofNullable(carteiras.get(id));
    }

    @Override
    public List<Carteira> consultarTodos() {
        return new ArrayList<>(carteiras.values());
    }

    @Override
    public void editar(Carteira carteira) {
        if (carteira == null) {
            throw new IllegalArgumentException(
                "A carteira não pode ser nula."
            );
        }

        int id = carteira.getId();

        if (!carteiras.containsKey(id)) {
            throw new NoSuchElementException(
                "Carteira não encontrada."
            );
        }

        carteiras.put(id, carteira);
    }

    @Override
    public void excluir(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException(
                "O identificador não pode ser nulo."
            );
        }

        if (!carteiras.containsKey(id)) {
            throw new NoSuchElementException(
                "Carteira não encontrada."
            );
        }

        carteiras.remove(id);
    }
}

