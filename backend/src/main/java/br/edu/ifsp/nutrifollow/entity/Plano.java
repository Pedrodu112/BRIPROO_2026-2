
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

        if (id != null) {
            throw new IllegalStateException(
                "Para incluir itens em um plano existente, informe o responsável."
            );
        }

        adicionarItemValidado(item);
    }

    public void incluirItem(
        ItemPlano item,
        Usuario responsavel,
        Acompanhamento acompanhamento) {

        validarPlanoAtivo();

        if (!podeSerAlteradoPor(responsavel, acompanhamento)) {
            throw new IllegalStateException(
                "O responsável não tem permissão para incluir itens neste plano."
            );
        }

        adicionarItemValidado(item);

        registrarAlteracao(responsavel, "Item incluído no plano.");
    }

    private void adicionarItemValidado(ItemPlano item) {
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
    }

    public void alterarItem(
        ItemPlano item,
        ItemPlano novosDados,
        Usuario responsavel,
        Acompanhamento acompanhamento) {

        validarPlanoAtivo();

        if (!podeSerAlteradoPor(responsavel, acompanhamento)) {
            throw new IllegalStateException(
                "O responsável não tem permissão para alterar este plano."
            );
        }

        if (item == null || novosDados == null) {
            throw new IllegalArgumentException(
                "O item e os novos dados são obrigatórios."
            );
        }

        if (!itens.contains(item)
            || !Boolean.TRUE.equals(item.getAtivo())) {
            throw new IllegalArgumentException(
                "O item não pertence ao plano ativo."
            );
        }

        item.atualizarConfiguracao(novosDados);

        registrarAlteracao(responsavel, "Item do plano alterado.");
    }

    public void removerItem(
        ItemPlano item,
        Usuario responsavel,
        Acompanhamento acompanhamento) {

        validarPlanoAtivo();

        if (!podeSerAlteradoPor(responsavel, acompanhamento)) {
            throw new IllegalStateException(
                "O responsável não tem permissão para remover itens deste plano."
            );
        }

        if (item == null
            || !itens.contains(item)
            || !Boolean.TRUE.equals(item.getAtivo())) {
            throw new IllegalArgumentException(
                "O item não pertence ao plano ou já está inativo."
            );
        }

        item.setAtivo(false);

        registrarAlteracao(
            responsavel,
            "Item inativado; histórico preservado."
        );
    }

    public boolean podeSerAlteradoPor(
        Usuario autor,
        Acompanhamento acompanhamento) {

        if (autor == null
            || this.usuario == null
            || !Boolean.TRUE.equals(this.ativo)
            || !Boolean.TRUE.equals(autor.getAtivo())
            || !Boolean.TRUE.equals(this.usuario.getAtivo())) {
            return false;
        }

        // O proprietário pode alterar somente seus próprios planos de treino.
        if (autor instanceof UsuarioComum) {
            return "TREINO".equalsIgnoreCase(this.tipo)
                && mesmaIdentidade(autor, this.usuario);
        }

        // Profissionais precisam de acompanhamento e autorização.
        if (!(autor instanceof Profissional profissional)
            || acompanhamento == null
            || acompanhamento.getUsuario() == null
            || acompanhamento.getProfissional() == null
            || !Boolean.TRUE.equals(
                acompanhamento.getUsuario().getAtivo())) {
            return false;
        }

        return mesmaIdentidade(this.usuario, acompanhamento.getUsuario())
            && mesmaIdentidade(
                profissional,
                acompanhamento.getProfissional())
            && profissional.estaVerificado()
            && profissional.podeGerenciarPlano(this.tipo)
            && acompanhamento.permiteAcesso(
                profissional,
                this.tipo,
                "ALTERAR"
            );
    }

    private boolean mesmaIdentidade(Usuario primeiro, Usuario segundo) {
    return primeiro != null
        && segundo != null
        && (primeiro == segundo
            || (primeiro.getId() != null
                && primeiro.getId().equals(segundo.getId())));
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
        validarPlanoAtivo();

        if (autor == null) {
            throw new IllegalArgumentException(
                "O autor da operação é obrigatório."
            );
        }

        if (!Boolean.TRUE.equals(autor.getAtivo())) {
            throw new IllegalArgumentException(
                "O autor precisa estar ativo."
            );
        }
        
            boolean ehDono =
                autor instanceof UsuarioComum
                && mesmaIdentidade(autor, this.usuario);

            boolean ehAdministrador =
                autor instanceof Administrador;

        if (!ehDono && !ehAdministrador) {
            throw new IllegalArgumentException(
                "Somente o dono do plano ou um administrador podem inativá-lo."
            );
        }

        this.ativo = false;
        registrarAlteracao(autor, "Plano inativado.");
    }

    private void validarPlanoAtivo() {
        if (!Boolean.TRUE.equals(this.ativo)) {
            throw new IllegalStateException("O plano está inativo.");
        }
    }

    private void registrarAlteracao(
        Usuario responsavel,
        String resumo) {

        if (responsavel == null
            || !Boolean.TRUE.equals(responsavel.getAtivo())) {
            throw new IllegalArgumentException(
                "O responsável pela alteração precisa estar ativo."
            );
        }

        if (resumo == null || resumo.isBlank()) {
            throw new IllegalArgumentException(
                "O resumo da alteração é obrigatório."
            );
        }

        String resumoNormalizado = resumo.trim();

        if (resumoNormalizado.length() > 1000) {
            throw new IllegalArgumentException(
                "O resumo não pode ultrapassar 1000 caracteres."
            );
        }

        this.versao = this.versao == null || this.versao < 1
            ? 1
            : this.versao + 1;

        this.atualizadoPor = responsavel;
        this.atualizadoEm = LocalDateTime.now();
        this.resumoUltimaAlteracao = resumoNormalizado;
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
