package bibliotheque.service;

import bibliotheque.repository.AmendeRepository;
import bibliotheque.repository.EmpruntRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class StatsService {

    private static final String[] MOIS = {
            "", "Jan", "Fév", "Mar", "Avr", "Mai", "Juin",
            "Juil", "Août", "Sep", "Oct", "Nov", "Déc"
    };

    @Autowired private EmpruntRepository empruntRepo;
    @Autowired private AmendeRepository amendeRepo;

    /**
     * Retourne les 12 derniers mois avec le nombre d'emprunts par mois.
     * Format : [liste de labels, liste de valeurs]
     */
    public Map<String, List<Object>> empruntsParMois() {
        // Créer une map [YYYY-MM] -> count
        Map<String, Long> data = new HashMap<>();
        for (Object[] row : empruntRepo.countEmpruntsParMois()) {
            int annee = ((Number) row[0]).intValue();
            int mois = ((Number) row[1]).intValue();
            long total = ((Number) row[2]).longValue();
            data.put(annee + "-" + mois, total);
        }

        // Générer les 12 derniers mois
        List<String> labels = new ArrayList<>();
        List<Long> valeurs = new ArrayList<>();

        LocalDate now = LocalDate.now();
        for (int i = 11; i >= 0; i--) {
            LocalDate d = now.minusMonths(i);
            String key = d.getYear() + "-" + d.getMonthValue();
            labels.add(MOIS[d.getMonthValue()] + " " + String.valueOf(d.getYear()).substring(2));
            valeurs.add(data.getOrDefault(key, 0L));
        }

        Map<String, List<Object>> result = new HashMap<>();
        result.put("labels", new ArrayList<>(labels));
        result.put("values", new ArrayList<>(valeurs));
        return result;
    }

    /** Top 5 livres : renvoie 2 listes [titres, counts] */
    public Map<String, List<Object>> top5Livres() {
        List<String> titres = new ArrayList<>();
        List<Long> counts = new ArrayList<>();

        for (Object[] row : empruntRepo.top5Livres()) {
            titres.add((String) row[0]);
            counts.add(((Number) row[1]).longValue());
        }

        // Si vide, mettre des placeholders
        if (titres.isEmpty()) {
            titres.add("Aucune donnée");
            counts.add(0L);
        }

        Map<String, List<Object>> result = new HashMap<>();
        result.put("labels", new ArrayList<>(titres));
        result.put("values", new ArrayList<>(counts));
        return result;
    }

    /** Top 5 utilisateurs */
    public Map<String, List<Object>> top5Utilisateurs() {
        List<String> noms = new ArrayList<>();
        List<Long> counts = new ArrayList<>();

        for (Object[] row : empruntRepo.top5Utilisateurs()) {
            noms.add((String) row[0]);
            counts.add(((Number) row[1]).longValue());
        }

        if (noms.isEmpty()) {
            noms.add("Aucune donnée");
            counts.add(0L);
        }

        Map<String, List<Object>> result = new HashMap<>();
        result.put("labels", new ArrayList<>(noms));
        result.put("values", new ArrayList<>(counts));
        return result;
    }

    /** Répartition par catégorie */
    public Map<String, List<Object>> empruntsParCategorie() {
        List<String> categories = new ArrayList<>();
        List<Long> counts = new ArrayList<>();

        for (Object[] row : empruntRepo.countParCategorie()) {
            categories.add((String) row[0]);
            counts.add(((Number) row[1]).longValue());
        }

        if (categories.isEmpty()) {
            categories.add("Aucune donnée");
            counts.add(0L);
        }

        Map<String, List<Object>> result = new HashMap<>();
        result.put("labels", new ArrayList<>(categories));
        result.put("values", new ArrayList<>(counts));
        return result;
    }

    /** Amendes par mois */
    public Map<String, List<Object>> amendesParMois() {
        Map<String, Double> data = new HashMap<>();
        for (Object[] row : amendeRepo.amendesParMois()) {
            int annee = ((Number) row[0]).intValue();
            int mois = ((Number) row[1]).intValue();
            double total = ((Number) row[2]).doubleValue();
            data.put(annee + "-" + mois, total);
        }

        List<String> labels = new ArrayList<>();
        List<Double> valeurs = new ArrayList<>();

        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate d = now.minusMonths(i);
            String key = d.getYear() + "-" + d.getMonthValue();
            labels.add(MOIS[d.getMonthValue()]);
            valeurs.add(data.getOrDefault(key, 0.0));
        }

        Map<String, List<Object>> result = new HashMap<>();
        result.put("labels", new ArrayList<>(labels));
        result.put("values", new ArrayList<>(valeurs));
        return result;
    }

    public long countEnRetard() { return empruntRepo.countEnRetard(); }

    public double totalAmendesImpayees() {
        Double t = amendeRepo.totalAmendesImpayees();
        return t != null ? t : 0.0;
    }

    public double totalAmendesPayees() {
        Double t = amendeRepo.totalAmendesPayees();
        return t != null ? t : 0.0;
    }
}