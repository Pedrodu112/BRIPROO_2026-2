
package br.edu.ifsp.nutrifollow.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
public class Plano {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioComum usuario;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private String titulo;

    private String objetivo;

    @OneToMany(mappedBy = "plano", cascade = CascadeType.ALL)
    private List<ItemPlano> itens = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "criado_por_id", nullable = false)
    private Usuario criadoPor;

    private LocalDateTime criadoEm;

    @ManyToOne
    @JoinColumn(name = "atualizado_por_id")
    private Usuario atualizadoPor;

    private LocalDateTime atualizadoEm;

    private Integer versao = 1;

    @Column(length = 1000)
    private String resumoUltimaAlteracao;

    private Boolean ativo = true;

    public Plano() {
    }

    public void incluirItem(ItemPlano item) {
        validarPlanoAtivo();

        if (item == null) {
            throw new IllegalArgumentException("O item é obrigatório.");
        }

        item.setPlano(this);

        if (!item.validarConfiguracao()) {
            item.setPlano(null);
            throw new IllegalArgumentException(
                "A configuração do item é inválida."
            );
        }

        if (Boolean.FALSE.equals(item.getAtivo())) {
            item.setPlano(null);
            throw new IllegalArgumentException(
                "Não é possível incluir um item inativo."
            );
        }

        itens.add(item);
        registrarAlteracao("Item incluído no plano.");
    }

    public void alterarItem(ItemPlano item, ItemPlano novosDados) {
        validarPlanoAtivo();

        if (item == null || novosDados == null) {
            throw new IllegalArgumentException(
                "O item e os novos dados são obrigatórios."
            );
        }

        if (!itens.contains(item) || !Boolean.TRUE.equals(item.getAtivo())) {
            throw new IllegalArgumentException(
                "O item não pertence ao plano ativo."
            );
        }

        item.atualizarConfiguracao(novosDados);
        registrarAlteracao("Item do plano alterado.");
    }

    public void removerItem(ItemPlano item) {
        validarPlanoAtivo();

        if (item == null
            || !itens.contains(item)
            || !Boolean.TRUE.equals(item.getAtivo())) {
            throw new IllegalArgumentException(
                "O item não pertence ao plano ou já está inativo."
            );
        }

        item.setAtivo(false);
        registrarAlteracao("Item inativado; histórico preservado.");
    }

    public boolean podeSerAlteradoPor(
        Usuario autor,
        Acompanhamento acompanhamento) {

        if (autor == null
            || acompanhamento == null
            || !Boolean.TRUE.equals(this.ativo)
            || !Boolean.TRUE.equals(autor.getAtivo())
            || !Boolean.TRUE.equals(acompanhamento.getUsuario().getAtivo())
            || !"ATIVO".equals(acompanhamento.getStatus())) {
            return false;
        }

        if (!this.usuario.equals(acompanhamento.getUsuario())) {
            return false;
        }

        if (autor instanceof UsuarioComum) {
            return this.usuario.equals(autor);
        }

        if (autor instanceof Profissional) {
            Profissional profissional = (Profissional) autor;

            return profissional.estaVerificado()
                && acompanhamento.getProfissional().equals(profissional)
                && profissional.podeGerenciarPlano(this.tipo)
                && acompanhamento.permiteAcesso(
                    profissional,
                    this.tipo,
                    "ALTERAR"
                );
        }

        return false;
    }

    public String descreverResumo() {
        long quantidadeItensAtivos = itens.stream()
            .filter(item -> Boolean.TRUE.equals(item.getAtivo()))
            .count();

        return "Plano: " + this.titulo
            + " | Tipo: " + this.tipo
            + " | Objetivo: " + this.objetivo
            + " | Quantidade de itens ativos: " + quantidadeItensAtivos
            + " | Versão: " + this.versao;
    }

    public void inativar(Usuario autor) {
        if (autor == null || !Boolean.TRUE.equals(autor.getAtivo())) {
            throw new IllegalArgumentException(
                "O autor da inativação precisa estar ativo."
            );
        }

        if (!Boolean.TRUE.equals(this.ativo)) {
            throw new IllegalStateException("O plano já está inativo.");
        }

        if (!(autor instanceof Administrador)
            && !(autor instanceof Profissional)
            && !this.usuario.equals(autor)) {
            throw new IllegalArgumentException(
                "O autor não tem permissão para inativar este plano."
            );
        }

        this.ativo = false;
        registrarAlteracao("Plano inativado.");
    }

    private void validarPlanoAtivo() {
        if (!Boolean.TRUE.equals(this.ativo)) {
            throw new IllegalStateException("O plano está inativo.");
        }
    }

    private void registrarAlteracao(String resumo) {
        this.versao++;
        this.atualizadoEm = LocalDateTime.now();
        this.resumoUltimaAlteracao = resumo;
    }

    public Integer getId() { return id; }
    public UsuarioComum getUsuario() { return usuario; }
    public void setUsuario(UsuarioComum usuario) { this.usuario = usuario; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getObjetivo() { return objetivo; }
    public void setObjetivo(String objetivo) { this.objetivo = objetivo; }
    public List<ItemPlano> getItens() {
        return Collections.unmodifiableList(itens);
    }
    public Usuario getCriadoPor() { return criadoPor; }
    public void setCriadoPor(Usuario criadoPor) { this.criadoPor = criadoPor; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public Usuario getAtualizadoPor() { return atualizadoPor; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public Integer getVersao() { return versao; }
    public String getResumoUltimaAlteracao() { return resumoUltimaAlteracao; }
    public Boolean getAtivo() { return ativo; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}
