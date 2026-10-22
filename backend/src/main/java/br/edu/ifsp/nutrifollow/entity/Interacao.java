
package br.edu.ifsp.nutrifollow.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;

import java.time.LocalDateTime;
import java.util.Locale;

@Entity
public class Interacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "acompanhamento_id", nullable = false)
    private Acompanhamento acompanhamento;

    @Column(nullable = false, length = 20)
    private String tipo;

    @ManyToOne
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @ManyToOne
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Usuario destinatario;

    @Column(nullable = false, length = 2000)
    private String conteudo;

    private LocalDateTime prazo;

    private String situacaoMeta;

    @Column(nullable = false)
    private LocalDateTime criadaEm;

    @Column(nullable = false)
    private LocalDateTime atualizadaEm;

    private LocalDateTime lidaEm;

    public Interacao() {
    }

    public boolean validarConteudo() {
        if (tipo == null || tipo.isBlank()
            || conteudo == null || conteudo.isBlank()
            || conteudo.trim().length() > 2000) {
            return false;
        }

        String tipoNormalizado = tipo.trim().toUpperCase(Locale.ROOT);

        if ("MENSAGEM".equals(tipoNormalizado)) {
            return true;
        }

        if ("META".equals(tipoNormalizado)) {
            return situacaoMeta != null
                && !situacaoMeta.isBlank();
        }

        if ("LEMBRETE".equals(tipoNormalizado)) {
            return prazo != null;
        }

        return false;
    }

    public void enviar() {
        if (criadaEm != null) {
            throw new IllegalStateException(
                "Esta interação já foi enviada."
            );
        }

        if (acompanhamento == null
            || autor == null
            || destinatario == null) {
            throw new IllegalStateException(
                "O acompanhamento, o autor e o destinatário são obrigatórios."
            );
        }

        if (!Boolean.TRUE.equals(autor.getAtivo())
            || !Boolean.TRUE.equals(destinatario.getAtivo())) {
            throw new IllegalStateException(
                "O autor e o destinatário precisam estar ativos."
            );
        }

        if (autor.equals(destinatario)) {
            throw new IllegalArgumentException(
                "O autor não pode ser o próprio destinatário."
            );
        }

        boolean autorParticipa =
            autor.equals(acompanhamento.getUsuario())
            || autor.equals(acompanhamento.getProfissional());

        boolean destinatarioParticipa =
            destinatario.equals(acompanhamento.getUsuario())
            || destinatario.equals(acompanhamento.getProfissional());

        if (!autorParticipa || !destinatarioParticipa) {
            throw new IllegalArgumentException(
                "Autor e destinatário precisam participar do acompanhamento."
            );
        }

        if ("MENSAGEM".equalsIgnoreCase(tipo)
            && !"ATIVO".equals(acompanhamento.getStatus())) {
            throw new IllegalStateException(
                "Mensagens exigem um acompanhamento ativo."
            );
        }

        if (!validarConteudo()) {
            throw new IllegalArgumentException(
                "O conteúdo da interação é inválido."
            );
        }

        LocalDateTime agora = LocalDateTime.now();

        criadaEm = agora;
        atualizadaEm = agora;
        lidaEm = null;
    }

    public void atualizar(String conteudo, LocalDateTime prazo) {
        if (criadaEm == null) {
            throw new IllegalStateException(
                "A interação precisa ser enviada antes de ser atualizada."
            );
        }

        if (conteudo == null || conteudo.isBlank()) {
            throw new IllegalArgumentException(
                "O conteúdo não pode ficar vazio."
            );
        }

        if (conteudo.trim().length() > 2000) {
            throw new IllegalArgumentException(
                "O conteúdo não pode ultrapassar 2000 caracteres."
            );
        }

        String tipoNormalizado = tipo == null
            ? ""
            : tipo.trim().toUpperCase(Locale.ROOT);

        if ("LEMBRETE".equals(tipoNormalizado) && prazo == null) {
            throw new IllegalArgumentException(
                "O lembrete precisa ter um prazo."
            );
        }

        if ("META".equals(tipoNormalizado)
            && (situacaoMeta == null || situacaoMeta.isBlank())) {
            throw new IllegalStateException(
                "A meta precisa ter uma situação válida."
            );
        }

        if (!"MENSAGEM".equals(tipoNormalizado)
            && !"META".equals(tipoNormalizado)
            && !"LEMBRETE".equals(tipoNormalizado)) {
            throw new IllegalStateException(
                "O tipo da interação é inválido."
            );
        }

        this.conteudo = conteudo.trim();
        this.prazo = prazo;
        this.atualizadaEm = LocalDateTime.now();
    }

    public void atualizarSituacaoMeta(String situacao) {
        if (!"META".equalsIgnoreCase(tipo)) {
            throw new IllegalStateException(
                "Somente uma interação do tipo META possui situação de meta."
            );
        }

        if (situacao == null || situacao.isBlank()) {
            throw new IllegalArgumentException(
                "A situação da meta é obrigatória."
            );
        }

        if (situacao.trim().length() > 50) {
            throw new IllegalArgumentException(
                "A situação da meta não pode ultrapassar 50 caracteres."
            );
        }

        this.situacaoMeta = situacao.trim();
        this.atualizadaEm = LocalDateTime.now();
    }

    public void marcarComoLida(Usuario destinatario) {
        if (destinatario == null
            || !destinatario.equals(this.destinatario)) {
            throw new IllegalArgumentException(
                "Somente o destinatário pode marcar a interação como lida."
            );
        }

        if (!Boolean.TRUE.equals(destinatario.getAtivo())) {
            throw new IllegalStateException(
                "O destinatário precisa estar ativo."
            );
        }

        if (criadaEm == null) {
            throw new IllegalStateException(
                "A interação ainda não foi enviada."
            );
        }

        if (lidaEm == null) {
            this.lidaEm = LocalDateTime.now();
        }
    }

    public Integer getId() {
        return id;
    }

    public Acompanhamento getAcompanhamento() {
        return acompanhamento;
    }

    public void setAcompanhamento(Acompanhamento acompanhamento) {
        this.acompanhamento = acompanhamento;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        if (tipo != null && tipo.trim().length() > 20) {
            throw new IllegalArgumentException(
                "O tipo não pode ultrapassar 20 caracteres."
            );
        }

        this.tipo = tipo == null ? null : tipo.trim();
    }

    public Usuario getAutor() {
        return autor;
    }

    public void setAutor(Usuario autor) {
        this.autor = autor;
    }

    public Usuario getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(Usuario destinatario) {
        this.destinatario = destinatario;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        if (conteudo != null && conteudo.trim().length() > 2000) {
            throw new IllegalArgumentException(
                "O conteúdo não pode ultrapassar 2000 caracteres."
            );
        }

        this.conteudo = conteudo == null ? null : conteudo.trim();
    }

    public LocalDateTime getPrazo() {
        return prazo;
    }

    public String getSituacaoMeta() {
        return situacaoMeta;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public LocalDateTime getAtualizadaEm() {
        return atualizadaEm;
    }

    public LocalDateTime getLidaEm() {
        return lidaEm;
    }
}
