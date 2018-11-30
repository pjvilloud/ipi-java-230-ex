package com.ipiecoles.java.java230;

import com.ipiecoles.java.java230.model.Employe;
import com.ipiecoles.java.java230.model.Manager;
import com.ipiecoles.java.java230.model.Technicien;
import com.ipiecoles.java.java230.repository.EmployeRepository;
import com.ipiecoles.java.java230.repository.ManagerRepository;
import com.ipiecoles.java.java230.repository.TechnicienRepository;
import org.joda.time.LocalDate;
//import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
//import java.util.Objects;

import java.util.List;

@Component
public class MyRunner implements CommandLineRunner {

    @Autowired
    private EmployeRepository employeRepository;

    @Autowired
    private TechnicienRepository technicienRepository;

    @Override
    public void run(String... strings) throws Exception {
        //Read
        //Employe e = employeRepository.findOne(10L);

        /*
        //Delete
        employeRepository.delete(9L);

        //Create
        Employe e2 = new Employe("Doe", "John", "X66666", LocalDate.now(), 2000.0);
        System.out.println(e2.getId());//Null
        e2 = employeRepository.save(e2);
        System.out.println(e2.getId());

        //update
        e2.setMatricule("X66667");
        e2 = employeRepository.save(e2);
        */

        //List<Employe> e4 = employeRepository.findAll();
        //List<Employe> e4 = employeRepository.findAll(new Sort(Sort.Direction.DESC, "matricule", "salaire"));

        /*
        Page<Employe> e4 = employeRepository.findAll(new PageRequest(1, 15, Sort.Direction.DESC, "matricule"));
        System.out.println("Nb employés : " + e4.getTotalElements());
        System.out.println("Nb pages : " + e4.getTotalPages());
        for (Employe emp : e4) {
            System.out.println(emp);
        }

        System.out.println("-------------------------------------------------");

        Page<Technicien> t1 = technicienRepository.findAll(new PageRequest(1, 15, Sort.Direction.DESC, "matricule"));
        System.out.println("Nb techniciens : " + t1.getTotalElements());
        System.out.println("Nb pages : " + t1.getTotalPages());
        for (Employe tech : t1) {
            if(tech instanceof Technicien) {
            ((Technicien)tech).getGrade();
        }


        Employe t = employeRepository.findByMatricule("T02141");
        Technicien technicien = (Technicien)t;
        System.out.println(technicien.getManager());

        Employe m = employeRepository.findByMatricule("M02149");
        Manager manager = (Manager)m;
        for (Technicien te : manager.getEquipe()) {
            System.out.println(te);
        }

        List <Technicien> t4 = technicienRepository.findByGradeAndManagerDateEmbaucheAfter(3, new LocalDate(2013, 1, 1));
            for(Technicien te : t4) {
                System.out.println(te);
                System.out.println(te.getManager().getDateEmbauche());
            }
        */

        //PageRequest e3 = new PageRequest(1, 5, Sort.Direction.DESC, "matricule");

        //Employe e4 = employeRepository.findByMatricule("C00004");

        Page<Employe> e5 = employeRepository.findByNomIgnoreCase("bARre", new PageRequest(0, 5));
        for (Employe employe : e5) {
            System.out.println(employe);
        }

        System.out.println("-------------------------------------------------");

        List<Employe> e6 = employeRepository.findByNomOrPrenomAllIgnoreCase("MaRie");
        for (Employe employe : e6) {
            System.out.println(employe);
        }
        //List<Employe> e5 = employeRepository.findByDateEmbaucheAfter(new LocalDate(2013, 5, 31));

        /*System.out.println("-------------------------------------------------");
        List<Employe> e7 = employeRepository.findEmployePlusRiches();
        for (Employe employe : e7) {
            System.out.println(employe);
        }*/

        System.out.println("-------------------------------------------------");

        Long e8 = employeRepository.countEmployePlusRiches();

        //print(e);
        //print(e3);
        print(e8);

    }
    public static void print(Object t) {
        System.out.println(t);

    }
}
