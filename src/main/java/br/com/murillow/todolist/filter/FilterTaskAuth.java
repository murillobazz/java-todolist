package br.com.murillow.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.murillow.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
public class FilterTaskAuth extends OncePerRequestFilter {
    
    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        var servletPath = request.getServletPath();

        if(servletPath.startsWith("/tasks/")) {
            // Pega a autenticação, e decripta o password

            var auth = request.getHeader("Authorization");

            var encodedAuth = auth.substring("Basic".length()).trim();
            byte[] decodedAuth =  Base64.getDecoder().decode(encodedAuth);
            var authString = new String(decodedAuth);

            String[] credentials = authString.split(":");
            String username = credentials[0];
            String password = credentials[1];

            // Valida username
            var user = this.userRepository.findByUsername(username);
            if (user == null) {
                response.sendError(401);
            } else {
                var passwordVerification = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
                if (passwordVerification.verified) {
                    request.setAttribute("userId", user.getId());
                    filterChain.doFilter(request, response);
                } else {
                    response.sendError(401);
                }
            }
        } else {
            filterChain.doFilter(request, response);
        }   
    }
}