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
    private static final String REGEX_TYPE = "^[MTC]{1}.*";



    @Autowired
    private ManagerRepository managerRepository;



    private List<Employe> employes = new ArrayList<Employe>();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void run(String... strings) throws Exception {
        String fileName = "employes.csv";
        readFile(fileName);
        //readFile(strings[0]);
    }

    /**
     * Méthode qui lit le fichier CSV en paramètre afin d'intégrer son contenu en BDD
     * @param fileName Le nom du fichier (à mettre dans src/main/resources)
     * @return une liste contenant les employés à insérer en BDD ou null si le fichier n'a pas pu être le
     */
    public List<Employe> readFile(String fileName) throws Exception {
        Stream<String> stream;
        stream = Files.lines(Paths.get(new ClassPathResource(fileName).getURI()));
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
        stream.close();
        return employes;
    }

    /**
     * Méthode qui regarde le premier caractère de la ligne et appelle la bonne méthode de création d'employé
     * @param ligne la ligne à analyser
     * @throws BatchException si le type d'employé n'a pas été reconnu
     * @throws Exception 
     */
    private void processLine(String ligne) throws BatchException, Exception {
        //TODO
    	
    	String[] parts = ligne.split(",");
    	
    	if (parts.length<4) {
    		if(parts[0].charAt(0) =='C') {
      		  processCommercial(ligne);
      	  }
      	  
      	  if(parts[0].charAt(0) =='T') {
      		  processTechnicien(ligne);
      	  }
      	  
      	  if(parts[0].charAt(0) =='M') {
      		  processManager(ligne);
      	  }	
    	}
    	
    	
    	  if(!parts[0].matches(REGEX_TYPE)) {
              throw new BatchException("Type d'employé inconnu : " + ligne.charAt(0));
          }
    	  
    	  if(parts[0].length()!=6) {
              throw new BatchException("la chaîne "+ parts[0] +" ne respecte pas l'expression régulière "+ REGEX_MATRICULE);
          }
    	  try {
    	  DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(parts[3]);
    	  }
    	  catch( Exception e) {
    		  throw new BatchException(parts[3]+" ne respecte pas le format de date dd/MM/yyyy"); 
    	  }
    	  
    	  try {
    		  Double.parseDouble(parts[4]);;
        	  }
        	  catch( Exception e) {
        		  throw new BatchException(parts[4]+" n'est pas un nombre valide pour un salaire "); 
        	  }
    	  if(parts[0].charAt(0) =='C') {
    		  processCommercial(ligne);
    	  }
    	  
    	  if(parts[0].charAt(0) =='T') {
    		  processTechnicien(ligne);
    	  }
    	  
    	  if(parts[0].charAt(0) =='M') {
    		  processManager(ligne);
    	  }
    	 
    		  

    	  
      
      }
    

    /**
     * Méthode qui crée un Commercial à partir d'une ligne contenant les informations d'un commercial et l'ajoute dans la liste globale des employés
     * @param ligneCommercial la ligne contenant les infos du commercial à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processCommercial(String ligneCommercial) throws BatchException {
    	String[] parts = ligneCommercial.split(",");
    	
    	if (parts.length!=7) {
    		throw new BatchException("La ligne commercial ne contient pas 7 éléments mais "+parts.length);	
    	}
    	
    	try {
  		  Integer.parseInt(parts[6]);
      	  }
      	  catch( Exception e) {
      		  throw new BatchException("La performance du commercial est incorrecte : "+parts[6]); 
      	  }
    	try {
  		  Double.parseDouble(parts[5]);
      	  }
      	  catch( Exception e) {
      		  throw new BatchException("Le chiffre d'affaire du commercial est incorrect : "+parts[5]); 
      	  }
    	 LocalDate dateEmbauche = DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(parts[3]);
    	Commercial c=new Commercial(parts[1], parts[2], parts[0], dateEmbauche,Double.parseDouble( parts[4]),
    			Double.parseDouble( parts[5]), Integer.parseInt(parts[6]));
    	
    }

    /**
     * Méthode qui crée un Manager à partir d'une ligne contenant les informations d'un manager et l'ajoute dans la liste globale des employés
     * @param ligneManager la ligne contenant les infos du manager à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processManager(String ligneManager) throws BatchException {
    	String[] parts = ligneManager.split(",");
        
    	if (parts.length!=5) {
    		throw new BatchException("La ligne manager ne contient pas 5 éléments mais "+parts.length);	
    	}
    	
    	 LocalDate dateEmbauche = DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(parts[3]);
    	Manager m = new Manager(parts[1], parts[2], parts[0], dateEmbauche,Double.parseDouble( parts[4]),null);
    	
    }

    /**
     * Méthode qui crée un Technicien à partir d'une ligne contenant les informations d'un technicien et l'ajoute dans la liste globale des employés
     * @param ligneTechnicien la ligne contenant les infos du technicien à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     * @throws TechnicienException 
     * @throws NumberFormatException 
     */
    private void processTechnicien(String ligneTechnicien) throws BatchException, NumberFormatException, TechnicienException {
    	String[] parts = ligneTechnicien.split(",");
    	
    	if (parts.length!=7) {
    		throw new BatchException("La ligne technicien ne contient pas 7 éléments mais "+parts.length);	
    	}
    	 try {
			  Integer.parseInt(parts[5]);
	  	  }
	  	  catch( Exception e) {
	  		  throw new BatchException("Le grade du technicien est incorrect : "+parts[5]); 
	  	  }
    	
    	 if ((Integer.parseInt(parts[5])<1 || Integer.parseInt(parts[5])>5)) {
			 
         	throw new BatchException("Le grade doit être compris entre 1 et 5 : "+parts[5]); 
         	  }
    	 
		  if(!parts[6].matches(REGEX_MATRICULE_MANAGER))
			  throw new BatchException("la chaîne "+parts[6]+" ne respecte pas l'expression régulière "+REGEX_MATRICULE_MANAGER); 
			  
		  
		  /*************************************************************/
		  Stream<String> stream;
		  boolean infile=false;
	        try {
				stream = Files.lines(Paths.get(new ClassPathResource("employes.csv").getURI()));
			
	        for(String ligneln : stream.collect(Collectors.toList())) {
	        	String[] partsln = ligneln.split(",");
	        	if(parts[6].equals(partsln[0])) {
	        		infile=true;
	        		return; 
	        	}
	        }
	        stream.close();
		  /***************************************************************/
		  
		  	Manager m=managerRepository.findByMatricule(parts[6]);	  
        	  if (m==null && infile==false)
        		  throw new BatchException("Le manager de matricule "+parts[6]+" n'a pas été trouvé dans le fichier ou en base de données "); 
	        } catch (IOException e1) {
				e1.printStackTrace();
			}  
	        LocalDate dateEmbauche = DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(parts[3]);
	        
				Technicien t =new Technicien(parts[1], parts[2], parts[0], dateEmbauche,Double.parseDouble(parts[4]),Integer.parseInt(parts[5]));
			
    }

}
