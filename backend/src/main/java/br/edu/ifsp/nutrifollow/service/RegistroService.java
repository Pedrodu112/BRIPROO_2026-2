
package br.edu.ifsp.nutrifollow.service;

import br.edu.ifsp.nutrifollow.entity.Registro;
import br.edu.ifsp.nutrifollow.entity.Usuario;
import br.edu.ifsp.nutrifollow.entity.UsuarioComum;
import br.edu.ifsp.nutrifollow.repository.RegistroRepository;
import br.edu.ifsp.nutrifollow.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistroService {

    private final RegistroRepository registroRepository;
    private final UsuarioRepository usuarioRepository;

    public RegistroService(
            RegistroRepository registroRepository,
            UsuarioRepository usuarioRepository) {
        this.registroRepository = registroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Registro registrar(Long usuarioId, Registro dados) {
        UsuarioComum usuario = buscarUsuarioAtivo(usuarioId);

        if (dados == null) {
            throw new IllegalArgumentException(
                    "Os dados do registro são obrigatórios."
            );
        }

        if (dados.getTipo() != null) {
            dados.setTipo(
                    dados.getTipo().trim().toUpperCase(Locale.ROOT)
            );
        }

        dados.setUsuario(usuario);
        dados.setRegistradoEm(LocalDateTime.now());

        if (!dados.validarDados()) {
            throw new IllegalArgumentException(
                    "Os dados do registro são inválidos."
            );
        }

        return registroRepository.save(dados);
    }

    @Transactional(readOnly = true)
    public List<Registro> listarPorUsuario(Long usuarioId) {
        buscarUsuarioAtivo(usuarioId);
        return registroRepository.findByUsuario_IdAndAtivoTrue(usuarioId);
    }

    @Transactional(readOnly = true)
    public Registro consultar(Long usuarioId, Integer registroId) {
        buscarUsuarioAtivo(usuarioId);
        return buscarRegistroDoUsuario(usuarioId, registroId);
    }

    @Transactional
    public Registro corrigir(
            Long usuarioId,
            Integer registroId,
            Registro novosDados) {

        buscarUsuarioAtivo(usuarioId);

        Registro registro = buscarRegistroDoUsuario(
                usuarioId, registroId
        );

        registro.corrigirRegistro(novosDados);

        return registroRepository.save(registro);
    }

    @Transactional
    public void inativar(Long usuarioId, Integer registroId) {
        buscarUsuarioAtivo(usuarioId);

        Registro registro = buscarRegistroDoUsuario(
                usuarioId, registroId
        );

        registro.inativar();

        registroRepository.save(registro);
    }

    private UsuarioComum buscarUsuarioAtivo(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException(
                    "O identificador do usuário é obrigatório."
            );
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuário não encontrado."
                ));

        if (!(usuario instanceof UsuarioComum usuarioComum)) {
            throw new IllegalArgumentException(
                    "A operação é permitida somente para usuários comuns."
            );
        }

        if (!Boolean.TRUE.equals(usuarioComum.getAtivo())) {
            throw new IllegalStateException(
                    "O usuário está inativo."
            );
        }

        return usuarioComum;
    }

    private Registro buscarRegistroDoUsuario(
            Long usuarioId,
            Integer registroId) {

        if (registroId == null) {
            throw new IllegalArgumentException(
                    "O identificador do registro é obrigatório."
            );
        }

        Registro registro = registroRepository.findById(registroId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Registro não encontrado."
                ));

        if (registro.getUsuario() == null
                || !usuarioId.equals(registro.getUsuario().getId())) {
            throw new IllegalArgumentException(
                    "Registro não encontrado para este usuário."
            );
        }

        if (!Boolean.TRUE.equals(registro.getAtivo())) {
            throw new IllegalStateException(
                    "O registro está inativo."
            );
        }

        return registro;
    }
}
