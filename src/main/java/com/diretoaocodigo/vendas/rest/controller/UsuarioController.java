package com.diretoaocodigo.vendas.rest.controller;

import com.diretoaocodigo.vendas.domain.entity.Usuario;
import com.diretoaocodigo.vendas.exception.SenhaInvalidaException;
import com.diretoaocodigo.vendas.rest.dto.CredencialDto;
import com.diretoaocodigo.vendas.rest.dto.TokenDto;
import com.diretoaocodigo.vendas.security.jwt.JwtService;
import com.diretoaocodigo.vendas.service.impl.UsuarioServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

@RestController
@Api("Api Usuários")
@RequiredArgsConstructor
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioServiceImpl usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("sCria um novo usuário")
    @ApiResponses({
            @ApiResponse(code = 201, message = "CREATED - Usuário criado com sucesso"),
            @ApiResponse(code = 400, message = "BAD_REQUEST - Erro(s) de validação"),
            @ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR")
    })
    public Usuario save(@RequestBody @Valid Usuario usuario) {
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        return usuarioService.save(usuario);
    }

    @PostMapping("/auth")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("Autentica um usuário existente")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK - Usuário autenticado com sucesso"),
            @ApiResponse(code = 401, message = "UNAUTHORIZED - Usuário nao autorizado"),
    })
    public TokenDto authenticate(@RequestBody CredencialDto credencialDTO) {
        try {
            Usuario usuario = Usuario.builder()
                    .login(credencialDTO.getLogin())
                    .senha(credencialDTO.getSenha())
                    .build();
            UserDetails usuarioAutenticado = usuarioService.authenticate(usuario);
            String token = jwtService.gerarToken(usuario);
            return new TokenDto(usuario.getLogin(), token);
        } catch (UsernameNotFoundException | SenhaInvalidaException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }
}