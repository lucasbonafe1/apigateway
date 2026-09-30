package example.redis.auth.repository;

import example.redis.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Usado no login: busca o usuário pelo username para validar a senha.
     *
     * Optional<User> em vez de User "cru" é uma prática do Spring Data:
     * força quem chama esse método a tratar explicitamente o caso
     * de "usuário não encontrado", em vez de arriscar um NullPointerException.
     */
    Optional<User> findByUsername(String username);

    /**
     * Usado no register: verifica duplicidade ANTES de tentar salvar.
     *
     * Poderíamos simplesmente tentar salvar e deixar o banco rejeitar
     * (por causa do UNIQUE constraint), mas aí o erro chegaria como uma
     * exceção genérica de SQL (DataIntegrityViolationException), que é
     * mais chato de tratar do que simplesmente checar antes.
     */
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}