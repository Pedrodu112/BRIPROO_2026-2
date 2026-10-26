
package br.edu.ifsp.nutrifollow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

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
    @JoinColumn(name = "destinatario_id")
    private Usuario destinatario;

    @Column(nullable = false, length = 2000)
    private String conteudo;

    private LocalDateTime prazo;

    @Column(length = 50)
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
            return acompanhamento != null
                && "ATIVO".equalsIgnoreCase(acompanhamento.getStatus())
                && autor != null
                && destinatario != null
                && Boolean.TRUE.equals(autor.getAtivo())
                && Boolean.TRUE.equals(destinatario.getAtivo())
                && !autor.equals(destinatario);
        }

        if ("OBSERVACAO".equals(tipoNormalizado)) {
            return autor instanceof Profissional
                && acompanhamento != null
                && autor.equals(acompanhamento.getProfissional());
        }

        if ("META".equals(tipoNormalizado)) {
            return autor instanceof Profissional
                && acompanhamento != null
                && autor.equals(acompanhamento.getProfissional())
                && situacaoMeta != null
                && !situacaoMeta.isBlank()
                && situacaoMeta.trim().length() <= 50;
        }

        return false;
    }

    public void enviar() {
        if (criadaEm != null) {
            throw new IllegalStateException(
                "Esta interação já foi enviada."
            );
        }

        if (acompanhamento == null || autor == null) {
            throw new IllegalStateException(
                "O acompanhamento e o autor são obrigatórios."
            );
        }

        if (!Boolean.TRUE.equals(autor.getAtivo())) {
            throw new IllegalStateException(
                "O autor precisa estar ativo."
            );
        }

        String tipoNormalizado = tipo == null
            ? ""
            : tipo.trim().toUpperCase(Locale.ROOT);

        if ("MENSAGEM".equals(tipoNormalizado)) {
            if (destinatario == null) {
                throw new IllegalStateException(
                    "A mensagem precisa ter um destinatário."
                );
            }

            if (!Boolean.TRUE.equals(destinatario.getAtivo())) {
                throw new IllegalStateException(
                    "O destinatário precisa estar ativo."
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

            if (!"ATIVO".equalsIgnoreCase(acompanhamento.getStatus())) {
                throw new IllegalStateException(
                    "Mensagens exigem um acompanhamento ativo."
                );
            }
        } else if ("OBSERVACAO".equals(tipoNormalizado)
            || "META".equals(tipoNormalizado)) {
            if (!(autor instanceof Profissional)) {
                throw new IllegalArgumentException(
                    "Somente um profissional pode criar observações e metas."
                );
            }

            if (destinatario != null) {
                throw new IllegalArgumentException(
                    "Observações e metas não devem ter destinatário."
                );
            }

            if (!autor.equals(acompanhamento.getProfissional())) {
                throw new IllegalArgumentException(
                    "O autor precisa ser o profissional deste acompanhamento."
                );
            }
        } else {
            throw new IllegalArgumentException(
                "O tipo da interação é inválido."
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

        String tipoNormalizado = tipo == null
            ? ""
            : tipo.trim().toUpperCase(Locale.ROOT);

        if ("MENSAGEM".equals(tipoNormalizado)) {
            throw new IllegalStateException(
                "Mensagens não podem ser editadas depois do envio."
            );
        }

        if (!"OBSERVACAO".equals(tipoNormalizado)
            && !"META".equals(tipoNormalizado)) {
            throw new IllegalStateException(
                "O tipo da interação é inválido."
            );
        }

        if (!"ATIVO".equalsIgnoreCase(acompanhamento.getStatus())) {
            throw new IllegalStateException(
                "Não é possível alterar interações após o encerramento do acompanhamento."
            );
        }

        if (!(autor instanceof Profissional)
            || !autor.equals(acompanhamento.getProfissional())) {
            throw new IllegalStateException(
                "Somente o profissional responsável pode alterar esta interação."
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

        if ("META".equals(tipoNormalizado)
            && (situacaoMeta == null || situacaoMeta.isBlank())) {
            throw new IllegalStateException(
                "A meta precisa ter uma situação válida."
            );
        }

        this.conteudo = conteudo.trim();
        this.prazo = "META".equals(tipoNormalizado) ? prazo : null;
        this.atualizadaEm = LocalDateTime.now();
    }

    public void atualizarSituacaoMeta(String situacao) {
        if (!"META".equalsIgnoreCase(tipo)) {
            throw new IllegalStateException(
                "Somente uma interação do tipo META possui situação de meta."
            );
        }

        if (criadaEm == null) {
            throw new IllegalStateException(
                "A meta precisa ser criada antes de atualizar sua situação."
            );
        }

        if (!"ATIVO".equalsIgnoreCase(acompanhamento.getStatus())) {
            throw new IllegalStateException(
                "Não é possível alterar metas após o encerramento do acompanhamento."
            );
        }

        if (!(autor instanceof Profissional)
            || !autor.equals(acompanhamento.getProfissional())) {
            throw new IllegalStateException(
                "Somente o profissional responsável pode atualizar a meta."
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
        if (!"MENSAGEM".equalsIgnoreCase(tipo)) {
            throw new IllegalStateException(
                "Somente mensagens podem ser marcadas como lidas."
            );
        }

        if (destinatario == null
            || !destinatario.equals(this.destinatario)) {
            throw new IllegalArgumentException(
                "Somente o destinatário pode marcar a mensagem como lida."
            );
        }

        if (!Boolean.TRUE.equals(destinatario.getAtivo())) {
            throw new IllegalStateException(
                "O destinatário precisa estar ativo."
            );
        }

        if (criadaEm == null) {
            throw new IllegalStateException(
                "A mensagem ainda não foi enviada."
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

    public void setPrazo(LocalDateTime prazo) {
        this.prazo = prazo;
    }

    public String getSituacaoMeta() {
        return situacaoMeta;
    }

    public void setSituacaoMeta(String situacaoMeta) {
        if (situacaoMeta != null && situacaoMeta.trim().length() > 50) {
            throw new IllegalArgumentException(
                "A situação da meta não pode ultrapassar 50 caracteres."
            );
        }

        this.situacaoMeta = situacaoMeta == null
            ? null
            : situacaoMeta.trim();
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
