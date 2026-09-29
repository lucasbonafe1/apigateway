package example.redis.auth.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RequestUtils {

    /**
     * Por que isso é complicado?
     *
     * Em produção, sua aplicação Spring Boot quase nunca recebe
     * a requisição diretamente do navegador do usuário. Normalmente
     * existe um Load Balancer, um Nginx, ou um CDN (Cloudflare) na frente.
     *
     * Isso significa que request.getRemoteAddr() vai retornar o IP
     * do proxy, não do cliente real!
     *
     * Por isso verificamos headers específicos que os proxies
     * costumam adicionar, antes de cair no fallback.
     */
    public String getClientIp(HttpServletRequest request) {

        // 1. X-Forwarded-For: pode conter uma CADEIA de IPs
        //    Exemplo: "cliente_real, proxy1, proxy2"
        //    O primeiro da lista é o mais confiável (mais próximo do cliente)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            String ip = xForwardedFor.split(",")[0].trim();
            log.debug("IP via X-Forwarded-For: {}", ip);
            return ip;
        }

        // 2. X-Real-IP: usado por Nginx quando configurado como reverse proxy
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            log.debug("IP via X-Real-IP: {}", xRealIp);
            return xRealIp;
        }

        // 3. Fallback: conexão direta (sem proxy) - comum em ambiente de dev
        String remoteAddr = request.getRemoteAddr();
        log.debug("IP via RemoteAddr (fallback): {}", remoteAddr);
        return remoteAddr;
    }
}