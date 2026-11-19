package br.com.athletiza.dao;

import br.com.athletiza.repositorio.ModalidadeRepositorio;
import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.modelo.Modalidade;
import br.com.athletiza.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ModalidadeDAO extends DAOBase implements ModalidadeRepositorio {

    @Override
    protected String getNomeEntidade() {
        return "a modalidade";
    }

    @Override
    public void inserir(Modalidade modalidade) throws PersistenciaException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(modalidade);
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            desfazer(em);
            throw traduzir(e, "incluir");
        } finally {
            em.close();
        }
    }

    @Override
    public void atualizar(Modalidade modalidade) throws PersistenciaException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(modalidade);
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            desfazer(em);
            throw traduzir(e, "salvar");
        } finally {
            em.close();
        }
    }

    @Override
    public void excluir(int id) throws PersistenciaException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Modalidade modalidade = em.find(Modalidade.class, id);
            if (modalidade != null) {
                em.remove(modalidade);
            }
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            desfazer(em);
            throw traduzir(e, "excluir");
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Modalidade> buscarPorId(int id) throws PersistenciaException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Modalidade.class, id));
        } catch (PersistenceException e) {
            throw traduzir(e, "consultar");
        } finally {
            em.close();
        }
    }

    @Override
    public List<Modalidade> listarTodos() throws PersistenciaException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT m FROM Modalidade m ORDER BY m.nome, m.genero", Modalidade.class)
                    .getResultList();
        } catch (PersistenceException e) {
            throw traduzir(e, "consultar");
        } finally {
            em.close();
        }
    }

    public boolean possuiVinculos(int id) throws PersistenciaException {
        return existe("SELECT (SELECT COUNT(*) FROM atleta_modalidade WHERE modalidade_id = ?)"
                + " + (SELECT COUNT(*) FROM treino WHERE modalidade_id = ?)"
                + " + (SELECT COUNT(*) FROM competicao_modalidade WHERE modalidade_id = ?)", id, id, id);
    }

    private void desfazer(EntityManager em) {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }

    private PersistenciaException traduzir(PersistenceException e, String operacao) {
        Throwable causa = e;
        while (causa != null) {
            if (causa instanceof SQLException erroSql) {
                return traduzir(erroSql, operacao);
            }
            causa = causa.getCause();
        }
        return new PersistenciaException("Erro ao " + operacao + " " + getNomeEntidade() + ". Tente novamente.", e);
    }
}
