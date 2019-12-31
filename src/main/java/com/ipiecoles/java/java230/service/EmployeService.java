package com.ipiecoles.java.java230.service;

import com.ipiecoles.java.java230.model.Employe;
import com.ipiecoles.java.java230.repository.EmployeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
public class EmployeService {

    @Autowired
    private EmployeRepository employeRepository;

    public Employe findById(Long id){
        Optional<Employe> employeOptional = employeRepository.findById(id);
        if(employeOptional.isPresent()){
            return employeOptional.get();
        }
        throw new EntityNotFoundException("Impossible de trouver l'employé d'identifiant " + id);
    }

    public Long countAllEmploye() {
        return employeRepository.count();
    }

    public void deleteEmploye(Long id){
        employeRepository.deleteById(id);
    }

    public Employe creerEmploye(Employe e) {
        return employeRepository.save(e);
    }
}
