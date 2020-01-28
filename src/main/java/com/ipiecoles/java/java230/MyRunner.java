package com.ipiecoles.java.java230;

import com.ipiecoles.java.java230.exceptions.BatchException;
import com.ipiecoles.java.java230.exceptions.TechnicienException;
import com.ipiecoles.java.java230.model.Commercial;
import com.ipiecoles.java.java230.model.Employe;
import com.ipiecoles.java.java230.model.Manager;
import com.ipiecoles.java.java230.model.Technicien;
import com.ipiecoles.java.java230.repository.CommercialRepository;
import com.ipiecoles.java.java230.repository.EmployeRepository;
import com.ipiecoles.java.java230.repository.ManagerRepository;
import com.ipiecoles.java.java230.repository.TechnicienRepository;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

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
    
    @Autowired
    private CommercialRepository commercialRepository;
    
    @Autowired
    private TechnicienRepository technicienRepository;

    private List<Employe> employes = new ArrayList<Employe>();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void run(String... strings) {
        String fileName = "employes.csv";
        readFile(fileName);
        //readFile(strings[0]);
    }

    /**
     * Méthode qui lit le fichier CSV en paramètre afin d'intégrer son contenu en BDD
     * @param fileName Le nom du fichier (à mettre dans src/main/resources)
     * @return une liste contenant les employés à insérer en BDD ou null si le fichier n'a pas pu être le
     */
    public List<Employe> readFile(String fileName) {
        Stream<String> stream = null;
        try {
			stream = Files.lines(Paths.get(new ClassPathResource(fileName).getURI()));
			
			//throw new IOException("moi");
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			
			System.out.println("Erreur ouverture du fichier");
			logger.error("Erreur ouverture du fichier");
		}
        
        //TODO
        Integer i = 0;
        
        for(String ligne : stream.collect(Collectors.toList())) {
            i++;
            try{
                processLine(ligne);
            }catch (BatchException e){
                System.out.println("Ligne " + i + " : " + e.getMessage() + " => " + ligne);
                

            }
        }

        return employes;
    }
    
    /**
     * Méthode qui regarde le premier caractère de la ligne et appelle la bonne méthode de création d'employé
     * @param ligne la ligne à analyser
     * @throws BatchException si le type d'employé n'a pas été reconnu
     */
    /*private void processLine(String ligne) throws BatchException {
        //TODO
        if(!ligne.matches("^[MTC]{1}.*")) {
            throw new BatchException("Type d'employé inconnu : " + ligne.charAt(0));
        }
    }*/

    /**Méthode qui regarde le premier caractère de la ligne et appelle la bonne méthode**/
    private void processLine(String ligne) throws BatchException {
    	if(!ligne.matches("^[MTC]{1}.*")) {
    		throw new BatchException("Type d'employé inconnu : " + ligne.charAt(0));
    	}
    	
    	String[] parts = ligne.split(",");
    	
    	if(!parts[0].matches(this.REGEX_MATRICULE)) {
    		throw new BatchException("La chaîne " + parts[0] + " ne respecte pas l'expression régulière : " + this.REGEX_MATRICULE);
    	}
    	
    	/*if(parts.length < 5) {
    		throw new BatchException("La ligne manager ne contient pas 5 éléments mais " + parts.length);
    	}*/
    	if(parts[0].charAt(0) == 'M') {
            if (parts.length != NB_CHAMPS_MANAGER) {
                throw new BatchException("La ligne manager ne contient pas " + NB_CHAMPS_MANAGER + " éléments mais " + parts.length);
            }
        }
        if(parts[0].charAt(0) == 'T') {
            if (parts.length != NB_CHAMPS_TECHNICIEN) {
                throw new BatchException("La ligne manager ne contient pas " + NB_CHAMPS_TECHNICIEN + " éléments mais " + parts.length);
            }
        }
        if(parts[0].charAt(0) == 'C') {
            if (parts.length != NB_CHAMPS_COMMERCIAL) {
                throw new BatchException("La ligne manager ne contient pas " + NB_CHAMPS_COMMERCIAL + " éléments mais " + parts.length);
            }
        }
        
        try {
    		DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(parts[3]);
    	} catch (Exception e) {
    		throw new BatchException("04/99/2013 ne respecte pas le format de date dd/MM/yyyy " + parts.length);
		}
        
        try {
            Double.parseDouble(parts[4]);
        } catch (Exception e) {
            throw new BatchException(parts[4] + " n'est pas un nombre valide pour un salaire");
        }
        
        if (parts[0].charAt(0) == 'C') {
        	try {
                Double.parseDouble(parts[5]);
            } catch (Exception e) {
                throw new BatchException("Le chiffre d'affaire du commercial est incorrect : " + parts[5]);
            }
        	try {
                Double.parseDouble(parts[6]);
            } catch (Exception e) {
                throw new BatchException("La performance du commercial est incorrecte : " + parts[6]);
            }
        }
        
        
        if (parts[0].charAt(0) == 'T') {
        	try {
                Integer.parseInt(parts[5]);
            } catch (Exception e) {
                throw new BatchException("Le grade du technicien est incorrect : " + parts[5]);
            }
        }
        
        int gradeMin = 1;
        int gradeMax = 5;
        
        if (parts[0].charAt(0) == 'T') {
        	// if (!(Integer.parseInt(parts[5]) >= gradeMin && Integer.parseInt(parts[5]) <= gradeMax)) {
        	if (Integer.parseInt(parts[5]) < gradeMin || Integer.parseInt(parts[5]) > gradeMax) {
        		throw new BatchException("Le grade doit être compris entre 1 et 5 : " + parts[5]);
            }

            if (!parts[6].matches(this.REGEX_MATRICULE_MANAGER)) {
                throw new BatchException("La chaîne " + parts[6] + " ne respecte pas l'expression régulière : " + this.REGEX_MATRICULE_MANAGER);
            }
            
            if (managerRepository.findByMatricule(parts[6]) == null) {
            	throw new BatchException("Le manager de matricule " + parts[6] + " n'a pas été trouvé dans le fichier  ou en base de données");
            }
        }
      	
        	
       if (parts[0].charAt(0) == 'C') {
        	processCommercial(ligne);
        } if (parts[0].charAt(0) == 'M') {
        	processManager(ligne);
        } /*if (parts[0].charAt(0) == 'T') {
        	processTechnicien(ligne);*/

        
    	
    	/*String date = "22/51/2019";
    	Commercial c = new Commercial();
    	try {
    		c.setDateEmbauche(DateTimeFormat.forPattern("dd/MM/YY").parseLocalDate(date));
    	} catch (Exception e) {
			// Problem de date
    		throw new BatchException("...");
		}*/
    }
    
    /**
     * Méthode qui crée un Commercial à partir d'une ligne contenant les informations d'un commercial et l'ajoute dans la liste globale des employés
     * @param ligneCommercial la ligne contenant les infos du commercial à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processCommercial(String ligneCommercial) throws BatchException {
        //TODO
    	String[] parts = ligneCommercial.split(",");
			Commercial commercial = new Commercial(parts[1], parts[2], parts[0], 
					DateTimeFormat.forPattern("dd/MM/YY").parseLocalDate(parts[3]), 
					Double.parseDouble(parts[4]), 
					Double.parseDouble(parts[5]));
			employeRepository.save(commercial);
			System.out.println("Le commercial " + commercial + " a été ajouté " );       
    }

    /**
     * Méthode qui crée un Manager à partir d'une ligne contenant les informations d'un manager et l'ajoute dans la liste globale des employés
     * @param ligneManager la ligne contenant les infos du manager à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processManager(String ligneManager) throws BatchException {
    	String[] parts = ligneManager.split(",");
		Manager manager = new Manager(parts[1], parts[2], parts[0], 
				DateTimeFormat.forPattern("dd/MM/YY").parseLocalDate(parts[3]), 
				Double.parseDouble(parts[4]), null);
		employeRepository.save(manager);
		System.out.println("Le manager " + manager + " a été ajouté " ); 

    }

    /**
     * Méthode qui crée un Technicien à partir d'une ligne contenant les informations d'un technicien et l'ajoute dans la liste globale des employés
     * @param ligneTechnicien la ligne contenant les infos du technicien à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processTechnicien(String ligneTechnicien) throws BatchException {
        //TODO
    	/*String[] parts = ligneTechnicien.split(",");
		Technicien technicien = null;
		try {
			technicien = new Technicien(parts[1], parts[2], parts[0], 
					DateTimeFormat.forPattern("dd/MM/YY").parseLocalDate(parts[3]),
					Double.parseDouble(parts[4]),
					Integer.parseInt(parts[5]));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TechnicienException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		employeRepository.save(technicien);
		System.out.println("Le technicien " + technicien + " a été ajouté " );*/
		
    }

}
