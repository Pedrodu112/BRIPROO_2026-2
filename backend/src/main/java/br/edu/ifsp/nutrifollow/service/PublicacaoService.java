
package br.edu.ifsp.nutrifollow.service;


import br.edu.ifsp.nutrifollow.entity.Profissional;
import br.edu.ifsp.nutrifollow.entity.Publicacao;
import br.edu.ifsp.nutrifollow.entity.Usuario;
import br.edu.ifsp.nutrifollow.repository.PublicacaoRepository;
import br.edu.ifsp.nutrifollow.repository.UsuarioRepository;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
    
@Service
public class PublicacaoService {

    private final PublicacaoRepository publicacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public PublicacaoService(
            PublicacaoRepository publicacaoRepository,
            UsuarioRepository usuarioRepository) {
        this.publicacaoRepository = publicacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Publicacao publicar(
            Long profissionalId,
            String titulo,
            String conteudo) {

        Profissional profissional = buscarProfissionalVerificado(
                profissionalId
        );

        Publicacao publicacao = profissional.publicarConteudo(titulo, conteudo);

        return publicacaoRepository.save(publicacao);
    }

    @Transactional(readOnly = true)
    public List<Publicacao> listarExibiveis() {
        return publicacaoRepository.findAll()
                .stream()
                .filter(Publicacao::podeSerExibida)
                .toList();
    }

    @Transactional
    public Publicacao editar(
            Long profissionalId,
            Long publicacaoId,
            String titulo,
            String conteudo) {

        Profissional profissional = buscarProfissionalVerificado(
                profissionalId
        );

        Publicacao publicacao = buscarPublicacao(publicacaoId);

        if (publicacao.getAutor() == null
                || !profissional.getId().equals(
                        publicacao.getAutor().getId())) {
            throw new IllegalArgumentException(
                    "Você não pode editar uma publicação de outro profissional."
            );
        }

        publicacao.editar(titulo, conteudo);

        return publicacaoRepository.save(publicacao);
          }

        @Transactional
        public void inativar(
                Long responsavelId,
                Long publicacaoId,
                String motivo) {

        if (responsavelId == null) {
                throw new IllegalArgumentException(
                "O identificador do responsável é obrigatório."
                );
        }

        Usuario responsavel = usuarioRepository.findById(responsavelId)
                .orElseThrow(() -> new IllegalArgumentException(
                "Responsável pela inativação não encontrado."
                ));

        Publicacao publicacao = buscarPublicacao(publicacaoId);

        publicacao.inativar(responsavel, motivo);

        publicacaoRepository.save(publicacao);
        }

    private Profissional buscarProfissionalVerificado(
            Long profissionalId) {

        if (profissionalId == null) {
            throw new IllegalArgumentException(
                    "O identificador do profissional é obrigatório."
            );
        }

        Usuario usuario = usuarioRepository.findById(profissionalId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Profissional não encontrado."
                ));

        if (!(usuario instanceof Profissional profissional)) {
            throw new IllegalArgumentException(
                    "O usuário informado não é um profissional."
            );
        }

        if (!Boolean.TRUE.equals(profissional.getAtivo())) {
            throw new IllegalStateException(
                    "O profissional está inativo."
            );
        }

        if (!profissional.estaVerificado()) {
            throw new IllegalStateException(
                    "O profissional ainda não foi verificado."
            );
        }

        return profissional;
    }

    private Publicacao buscarPublicacao(Long publicacaoId) {
        if (publicacaoId == null) {
            throw new IllegalArgumentException(
                    "O identificador da publicação é obrigatório."
            );
        }

        return publicacaoRepository.findById(publicacaoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Publicação não encontrada."
                ));
    }
}
