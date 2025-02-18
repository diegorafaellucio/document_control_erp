package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.repository.BiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BiService {
    @Autowired private BiRepository biRepository;
}
