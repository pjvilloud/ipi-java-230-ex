package com.ipiecoles.java.java230;

import com.ipiecoles.java.java230.exceptions.BatchException;
import com.ipiecoles.java.java230.exceptions.TechnicienException;
import com.ipiecoles.java.java230.model.Commercial;
import com.ipiecoles.java.java230.model.Employe;
import com.ipiecoles.java.java230.model.Manager;
import com.ipiecoles.java.java230.model.Technicien;
import com.ipiecoles.java.java230.repository.EmployeRepository;
import com.ipiecoles.java.java230.repository.ManagerRepository;

import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jdk.nashorn.internal.runtime.regexp.joni.Regex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class MyRunner implements CommandLineRunner {

    private static final String REGEX_MATRICULE = "^[MTC][0-9]{5}$";
    private static final String REGEX_NOM = ".*";
    private static final String REGEX_PRENOM = ".*";
    private static final int NB_CHAMPS_MANAGER = 5;
    private static final int NB_CHAMPS_TECHNICIEN = 7;
    private static final String REGEX_MATRICULE_MANAGER = "^M[0-9]{5}$";
    private static final int NB_CHAMPS_COMMERCIAL = 7;

    @Autowired
    private EmployeRepository employeRepository;

    @Autowired
    private ManagerRepository managerRepository;

    private final List<Employe> employes = new ArrayList<Employe>();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void run(final String... strings) {
        final String fileName = "employes.csv";
        readFile(fileName);
        // readFile(strings[0]);
    }

    /**
     * Méthode qui lit le fichier CSV en paramètre afin d'intégrer son contenu en
     * BDD
     * 
     * @param fileName Le nom du fichier (à mettre dans src/main/resources)
     * @return une liste contenant les employés à insérer en BDD ou null si le
     *         fichier n'a pas pu être le
     */
    public List<Employe> readFile(final String fileName) {
        Stream<String> stream;
        try {
			stream = Files.lines(Paths.get(new ClassPathResource(fileName).getURI()));
		} catch (IOException e1) {
			logger.error("Impossible de lire le fichier " + fileName);
			return new ArrayList<Employe>();
		}
      
        Integer i = 0;
        for (final String ligne : stream.collect(Collectors.toList())) {
            i++;
            try {
                processLine(ligne);
            } catch (final BatchException e) {
                logger.error("Ligne " + i + " : " + e.getMessage() + " => " + ligne);
            }
        }

        return employes;
    }

    /**
     * Méthode qui regarde le premier caractère de la ligne et appelle la bonne
     * méthode de création d'employé
     * 
     * @param ligne la ligne à analyser
     * @throws BatchException si le type d'employé n'a pas été reconnu
     */
    private void processLine(final String ligne) throws BatchException {
        
        if (!ligne.matches("^[MTC]{1}.*")) {
            throw new BatchException("Type d'employé inconnu : " + ligne.charAt(0));
        }
        
        // ligne : String :kjkljl,kjhkj,kjh
        // ["kjkljl", "", ""]
        final String[] tab = ligne.split(",");
        
        if (tab.length < 5) {
        	throw new BatchException ("La ligne Employe ne contient pas tous les champs !");
        }

        // matricule :
        final String matricule = tab[0];
        
        if (!matricule.matches(REGEX_MATRICULE)) {
        	throw new BatchException ("La chaîne " + matricule + " ne respecte pas l'expression régulière " + REGEX_MATRICULE);
        }
        
        Double salaire = null;
        try {
            salaire = Double.parseDouble(tab[4]);
        }
        catch (NumberFormatException e1) {
        	
            throw new BatchException(tab[4] + " n'est pas un nombre valide pour un salaire");
                
        }
        
        String date = tab[3];
        try { 
        	DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(date);
        }
        catch (IllegalArgumentException e1){
        	// problem de date
        	throw new BatchException(date + " ne respecte pas le format de date dd/MM/yyyy");
        	
        }

        if (matricule.startsWith("C")) {
            processCommercial(ligne, tab);
            
        } else if (matricule.startsWith("T")) {
            processTechnicien(ligne, tab);
        } else if (matricule.startsWith("M")) {
            processManager(ligne, tab);

        }

    }

    /**
     * Méthode qui crée un Commercial à partir d'une ligne contenant les
     * informations d'un commercial et l'ajoute dans la liste globale des employés
     * 
     * @param ligneCommercial la ligne contenant les infos du commercial à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processCommercial(final String ligneCommercial, final String[] tableau) throws BatchException {
        // TODO
        //
    	
    	Commercial c = new Commercial();
    	c.setMatricule(tableau[0]);
    	c.setNom(tableau[1]);
    	c.setPrenom(tableau[2]);
    	c.setDateEmbauche(DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(tableau[3]));
    	c.setSalaire(Double.parseDouble(tableau[4]));
    	
    	if (!(tableau.length==NB_CHAMPS_COMMERCIAL)) {
        	
        	throw new BatchException("La ligne commercial ne contient pas " + NB_CHAMPS_COMMERCIAL + " éléments mais " + tableau.length);
        	
        } 
        
        Double chiffreAffaire = null;
        try {
        	chiffreAffaire = Double.parseDouble(tableau[5]);
        }
        catch (NumberFormatException e1) {
        	
            throw new BatchException("Le chiffre d'affaire du commercial est incorrect : " + tableau[5]);
                
        } 
        c.setCaAnnuel(chiffreAffaire);
        
        Integer performance = null;
        try {
        	performance = Integer.parseInt(tableau[6]);
        }
        catch (NumberFormatException e1) {
        	
            throw new BatchException("La performance du commercial est incorrecte : " + tableau[6]);
                
        } 
        c.setPerformance(performance);
        
        employes.add(c);
    }

  
    
    /**
     * Méthode qui crée un Manager à partir d'une ligne contenant les informations
     * d'un manager et l'ajoute dans la liste globale des employés
     * 
     * @param ligneManager la ligne contenant les infos du manager à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processManager(final String ligneManager, final String[] tableau) throws BatchException {
        // TODO
    	/* Ne sert à rien car fait déjà avant donc aucune chance de passer ce test!!!!
        if (!tableau[0].matches(REGEX_MATRICULE_MANAGER)) {
            // 
            throw new BatchException("la chaîne" + tableau[0] + " ne respecte pas l'expression régulière ^[MTC][0-9]{5}$  : " + ligneManager.charAt(0));
                
        } 
        */
        
    	Manager m = new Manager();
    	
        if (!(tableau.length==NB_CHAMPS_MANAGER)) {
        	
        	throw new BatchException("La ligne manager ne contient pas " + NB_CHAMPS_MANAGER + " éléments mais " + tableau.length);
        	
        }
        
        m.setMatricule(tableau[0]);
    	m.setNom(tableau[1]);
    	m.setPrenom(tableau[2]);
    	m.setDateEmbauche(DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(tableau[3]));
    	m.setSalaire(Double.parseDouble(tableau[4]));
        
    	employes.add(m);
    }

    /**
     * Méthode qui crée un Technicien à partir d'une ligne contenant les
     * informations d'un technicien et l'ajoute dans la liste globale des employés
     * 
     * @param ligneTechnicien la ligne contenant les infos du technicien à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processTechnicien(final String ligneTechnicien, final String[] tableau) throws BatchException {
        //TODO
    	Technicien t = new Technicien();
    	
    	t.setMatricule(tableau[0]);
    	t.setNom(tableau[1]);
    	t.setPrenom(tableau[2]);
    	t.setDateEmbauche(DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(tableau[3]));
    	
        
    	if (!(tableau.length==NB_CHAMPS_TECHNICIEN)) {
        	
        	throw new BatchException("La ligne manager ne contient pas " + NB_CHAMPS_TECHNICIEN + " éléments mais " + tableau.length);
        	
        }

        
    	Integer grade = null;
        try {
            grade = Integer.parseInt(tableau[5]);
        }
        catch (NumberFormatException e1) {
        	
            throw new BatchException("Le grade du technicien est incorrect : " + tableau[5] );
                
        } 
        
        
        try {
        	 t.setGrade(grade);
        }
        catch (TechnicienException e1) {
        	throw new BatchException("Le grade doit être compris entre 1 et 5 : " + grade);
            
        }
        
        t.setSalaire(Double.parseDouble(tableau[4]));
        
        // la chaîne xxx ne respecte pas l'expression régulière ^M[0-9]{5}$
        // String chaine = null;
        
        if (!tableau[6].matches(REGEX_MATRICULE_MANAGER)) {
        	
        	throw new BatchException("La chaîne " + tableau[6] + " ne respecte pas l'expression régulière " + REGEX_MATRICULE_MANAGER );
            
        }
        
        
       Manager m = managerRepository.findByMatricule(tableau[6]); 
      
        if (m == null) {
        	for (Employe e : employes) {
        		if (e.getMatricule().equals(tableau[6])) {
        			m = (Manager)e;
        		}
        	}
        }
        
        if (m == null) {
           	throw new BatchException("Le manager de matricule "+ tableau[6] + " n'a pas été trouvé dans le fichier ou en base de données");
        }
        
        t.setManager(m);
        
        employes.add(t);
        
    }

}
