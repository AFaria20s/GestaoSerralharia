package com.afonso.gestaoSerralharia.desktop.api;

import com.afonso.gestaoSerralharia.models.Dono;

public record AuthApiResponse(
        Dono dono,
        String nome,
        String role,
        String painelInicial
) {
}
