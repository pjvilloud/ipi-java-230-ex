package com.ipiecoles.java.java230;

import com.ipiecoles.java.java230.exceptions.BatchException;
import com.ipiecoles.java.java230.model.Employe;
import com.ipiecoles.java.java230.model.Manager;
import com.ipiecoles.java.java230.model.Technicien;
import com.ipiecoles.java.java230.model.Commercial;
import com.ipiecoles.java.java230.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.IOException;

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

    private List<Employe> employes = new ArrayList<>();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private List<String> lignes;

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
    public List<Employe> readFile(String fileName) {

        Stream<String> stream;
        //Catcher l'exception en cas de fichier non trouvé
        try {
            stream = Files.lines(Paths.get(new ClassPathResource(fileName).getURI()));
        }
        catch(IOException e) {
            logger.error("Problèmè dans l'ouverture du fichier " + fileName);
            return null;
        }
        //Afficher chaque ligne du fichier dans la console
        lignes = stream.collect(Collectors.toList());
        for (int i = 0; i < lignes.size(); i++) {
            //System.out.println(lignes.get(i));
            try {
                processLine(lignes.get(i));
            } catch (BatchException e) {
                logger.error("Ligne " + (i + 1) + " : " + e.getMessage());
                //On passe à la ligne suivante
            }
        }
        return employes;
    }

    /**
     * Méthode qui regarde le premier caractère de la ligne et appelle la bonne méthode de création d'employé
     * @param ligne la ligne à analyser
     * @throws BatchException si le type d'employé n'a pas été reconnu
     */
    private void processLine(String ligne) throws BatchException {
        String charZero = Character.toString(ligne.charAt(0));
        switch (charZero) {
            case "C":
                processCommercial(ligne);
                break;
            case "M":
                processManager(ligne);
                break;
            case "T":
                processTechnicien(ligne);
                break;
            default:
                throw new BatchException("Type d'employé inconnu : " + charZero + " => " + ligne);
        }
    }

    /**
     * Methode qui vérifie que le nombre des champs contenues dans la ligne correspond bien au nombre des champs du type d'employé
     * @param ligne
     * @param type
     * @param NbChamps
     * @throws BatchException
     */
    private void checkNombreChamps(String ligne, String type, Integer NbChamps) throws BatchException {
        String[] champs = ligne.split(",");
        if(champs.length != NbChamps) {
            throw new BatchException("La ligne "+type+" ne contient pas "+NbChamps+" éléments mais " + champs.length + " => " + ligne);
        }
    }

    /**
     * Méthode qui vérifie que la matricule respecte bien l'expression régulière regex prédéfinie
     * @param ligne
     * @param index
     * @param regex
     * @throws BatchException
     */
    private void checkMatricule(String ligne, Integer index, String regex) throws BatchException {
        String[] champs = ligne.split(",");
        String matricule = champs[index];
        if(!matricule.matches(regex)) {
            throw new BatchException("la chaîne " + matricule + " ne respecte pas l'expression régulière "+regex+" => " + ligne);
        }
    }

    /**
     * Méthode qui retourne un Double représenté par un String ou lève une exception
     * si le String ne peut pas être parsé
     * @param ligne
     * @param index
     * @return
     * @throws BatchException
     */
    private Double StringToDouble(String ligne, Integer index) throws BatchException {
        String[] champs = ligne.split(",");
        String monDouble = champs[index];
        //if(Double.valueOf(monSalaire))
        try {
            return Double.parseDouble(monDouble);
        } catch (NumberFormatException e) {
            if (index == 4)
                throw new BatchException(monDouble+" n'est pas un nombre valide pour un salaire => "+ligne);
            else
                throw new BatchException("Le chiffre d'affaire du commercial est incorrect : " + monDouble + " => " + ligne);
        }
    }

    /**
     * Méthode qui retourne un Integer représenté par un String ou lève une exception
     * si le String ne peut pas être parsé
     * @param ligne
     * @param index
     * @return
     * @throws BatchException
     */
    private Integer StringToInteger(String ligne, Integer index) throws BatchException {
        String[] champs = ligne.split(",");
        String monInteger = champs[index];
        try {
            return Integer.parseInt(monInteger);
        } catch (NumberFormatException e) {
            if (index == 5)
                throw new BatchException("Le grade du technicien est incorrect : "+monInteger+" => "+ligne);
            else
                throw new BatchException("La performance du commercial est incorrecte : "+monInteger+" => "+ligne);
        }
    }

    /**
     * Méthode qui retourne un LocalDate représenté par une Date du format "dd/MM/yyyy" ou lève une exception
     * si la Date n'est pas au bon format
     * @param ligne
     * @return
     * @throws BatchException
     */
    private LocalDate dateToLocalDate(String ligne) throws BatchException {
        String[] champs = ligne.split(",");
        String monDate = champs[3];
        try {
            return DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(monDate);
        } catch (Exception e){
            throw new BatchException(monDate+" ne respecte pas le format de date dd/MM/yyyy => "+ligne);
        }
    }

    /**
     * Méthode qui cherche si le matricule d'un Manager est présente soit dans le fichier µ.csv soit en base de données
     * @param ligne
     * @throws BatchException
     */
    private void checkManager(String ligne) throws BatchException {
        String[] champs = ligne.split(",");
        String matricule = champs[6];
        Boolean check = false;
        for (int i = 0; i < lignes.size(); i++) {
            String temp = lignes.get(i);
            String[] champs2 = temp.split(",");
            if (matricule.equals(champs2[0])) {
                check = true;
            }
        }
        List<Employe> list = employeRepository.findByMatricule(matricule);
        if(!check && list.size() == 0) {
            throw new BatchException("Le manager de matricule "+matricule+" n'a pas été trouvé dans le fichier ou en base de données => " + ligne);
        }
    }

    /**
     * Méthode qui crée un Commercial à partir d'une ligne contenant les informations d'un commercial et l'ajoute dans la liste globale des employés
     * @param ligneCommercial la ligne contenant les infos du commercial à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processCommercial(String ligneCommercial) throws BatchException {
        String[] champs = ligneCommercial.split(",");
        checkNombreChamps(ligneCommercial, "commercial", NB_CHAMPS_COMMERCIAL);
        checkMatricule(ligneCommercial, 0, REGEX_MATRICULE);
        LocalDate dateEmbauche = dateToLocalDate(ligneCommercial);
        Double salaire = StringToDouble(ligneCommercial, 4);
        Double caAnnuel = StringToDouble(ligneCommercial, 5);
        Integer performance = StringToInteger(ligneCommercial, 6);

        // sauvegarder un nouveau commercial dans la BDD
        Commercial c = new Commercial(champs[1], champs[2], champs[0], dateEmbauche, salaire, caAnnuel, performance);
        employeRepository.save(c);
    }

    /**
     * Méthode qui crée un Manager à partir d'une ligne contenant les informations d'un manager et l'ajoute dans la liste globale des employés
     * @param ligneManager la ligne contenant les infos du manager à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processManager(String ligneManager) throws BatchException {
        String[] champs = ligneManager.split(",");
        checkNombreChamps(ligneManager, "manager", NB_CHAMPS_MANAGER);
        checkMatricule(ligneManager, 0, REGEX_MATRICULE);
        LocalDate dateEmbauche = dateToLocalDate(ligneManager);
        Double salaire = StringToDouble(ligneManager, 4);

        // sauvegarder un nouveau manager dans la BDD
        Manager m = new Manager(champs[1], champs[2], champs[0], dateEmbauche, salaire);
        employeRepository.save(m);
    }

    /**
     * Méthode qui crée un Technicien à partir d'une ligne contenant les informations d'un technicien et l'ajoute dans la liste globale des employés
     * @param ligneTechnicien la ligne contenant les infos du technicien à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processTechnicien(String ligneTechnicien) throws BatchException {
        String[] champs = ligneTechnicien.split(",");
        checkNombreChamps(ligneTechnicien, "technicien", NB_CHAMPS_TECHNICIEN);
        checkMatricule(ligneTechnicien, 0, REGEX_MATRICULE);
        checkMatricule(ligneTechnicien, 6, REGEX_MATRICULE_MANAGER);
        LocalDate dateEmbauche = dateToLocalDate(ligneTechnicien);
        Double salaire = StringToDouble(ligneTechnicien, 4);
        Integer gradeTest = StringToInteger(ligneTechnicien, 5);
        Integer grade = null;
        if (gradeTest != null) {
            if (gradeTest >= 1 && gradeTest <= 5) {
                grade = gradeTest;
            } else
                throw new BatchException("Le grade doit être compris entre 1 et 5 : " + gradeTest + " => " + ligneTechnicien);
        }
        checkManager(ligneTechnicien);
        String manager_matr = champs[6];

        // sauvegarder un nouveau technicien dans la BDD
        Technicien t = new Technicien(champs[1], champs[2], champs[0], dateEmbauche, salaire, grade);
        employeRepository.save(t);
    }
}
