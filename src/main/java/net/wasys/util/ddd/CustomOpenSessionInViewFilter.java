package net.wasys.util.ddd;

import net.wasys.util.other.Bolso;
import org.springframework.orm.hibernate5.support.OpenSessionInViewFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CustomOpenSessionInViewFilter extends OpenSessionInViewFilter {

    private static final List<Bolso<Thread>> sessionThreadList = new CopyOnWriteArrayList<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        Thread thread = Thread.currentThread();
        Bolso<Thread> bolso = new Bolso<>();
        bolso.setObjeto(thread);
        bolso.setStartTime(System.currentTimeMillis());

        try {
            sessionThreadList.add(bolso);

            boolean abrirSession = getAbrirSession(request);
            if(abrirSession) {
                super.doFilterInternal(request, response, filterChain);
            } else {
                filterChain.doFilter(request, response);
            }
        }
        finally {
            sessionThreadList.remove(bolso);
        }
    }

    private boolean getAbrirSession(HttpServletRequest request) {

        String requestURI = request.getRequestURI();
        boolean abrirSession = !requestURI.contains("/resources/")
                && !requestURI.contains("/javax.faces.resource/");

        return abrirSession;
    }

    public static List<Bolso<Thread>> getSessionThreadList() {
        return sessionThreadList;
    }
}
