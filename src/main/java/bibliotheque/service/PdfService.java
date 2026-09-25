package bibliotheque.service;

import bibliotheque.modele.*;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color PRIMARY = new Color(99, 102, 241);
    private static final Color GRAY_HEADER = new Color(240, 241, 245);
    private static final Color GRAY_TEXT = new Color(107, 114, 128);
    private static final Color SUCCESS = new Color(16, 185, 129);
    private static final Color DANGER = new Color(239, 68, 68);

    // ==================== RAPPORT LIVRES ====================

    public byte[] genererRapportLivres(List<Livre> livres) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 40, 40, 60, 40);
            PdfWriter.getInstance(document, baos);
            document.open();

            ajouterHeader(document, "📚 Catalogue des livres",
                    "Total : " + livres.size() + " livre(s)");

            PdfPTable table = new PdfPTable(new float[]{1, 4, 3, 2, 1.5f, 3});
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);

            ajouterEntetes(table, "ID", "Titre", "Auteur", "Catégorie", "Année", "ISBN");

            for (Livre l : livres) {
                ajouterCellule(table, String.valueOf(l.getId()), Element.ALIGN_CENTER);
                ajouterCellule(table, l.getTitre(), Element.ALIGN_LEFT);
                ajouterCellule(table, l.getAuteur(), Element.ALIGN_LEFT);
                ajouterCellule(table, l.getCategorie() != null ? l.getCategorie() : "—", Element.ALIGN_CENTER);
                ajouterCellule(table, l.getAnneePublication() != null
                        ? String.valueOf(l.getAnneePublication()) : "—", Element.ALIGN_CENTER);
                ajouterCellule(table, l.getIsbn() != null ? l.getIsbn() : "—", Element.ALIGN_LEFT);
            }

            document.add(table);
            ajouterFooter(document);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF livres", e);
        }
    }

    // ==================== RAPPORT UTILISATEURS ====================

    public byte[] genererRapportUtilisateurs(List<Utilisateur> users) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 40, 40, 60, 40);
            PdfWriter.getInstance(document, baos);
            document.open();

            ajouterHeader(document, "👥 Liste des utilisateurs",
                    "Total : " + users.size() + " utilisateur(s)");

            PdfPTable table = new PdfPTable(new float[]{1, 3, 4, 2.5f, 2});
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);

            ajouterEntetes(table, "ID", "Nom", "Email", "Rôle", "Amendes");

            for (Utilisateur u : users) {
                ajouterCellule(table, String.valueOf(u.getId()), Element.ALIGN_CENTER);
                ajouterCellule(table, u.getNom(), Element.ALIGN_LEFT);
                ajouterCellule(table, u.getEmail(), Element.ALIGN_LEFT);
                ajouterCellule(table, u.getRoleLibelle(), Element.ALIGN_CENTER);

                Double solde = u.getSoldeAmendes() != null ? u.getSoldeAmendes() : 0.0;
                PdfPCell cellAmende = new PdfPCell(new Phrase(
                        String.format("%.2f €", solde),
                        new Font(Font.HELVETICA, 9, Font.BOLD,
                                solde > 0 ? DANGER : SUCCESS)));
                cellAmende.setHorizontalAlignment(Element.ALIGN_CENTER);
                cellAmende.setPadding(6);
                cellAmende.setBorderColor(GRAY_HEADER);
                table.addCell(cellAmende);
            }

            document.add(table);
            ajouterFooter(document);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF utilisateurs", e);
        }
    }

    // ==================== RAPPORT EMPRUNTS ====================

    public byte[] genererRapportEmprunts(List<Emprunt> emprunts) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 40, 40, 60, 40);
            PdfWriter.getInstance(document, baos);
            document.open();

            long enRetard = emprunts.stream().filter(e -> e.joursDeRetard() > 0).count();

            ajouterHeader(document, "📋 Emprunts en cours",
                    "Total : " + emprunts.size() + " emprunt(s) | "
                    + enRetard + " en retard");

            PdfPTable table = new PdfPTable(new float[]{1, 4, 3, 2, 2, 1.5f});
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);

            ajouterEntetes(table, "ID", "Livre", "Emprunteur",
                    "Date emprunt", "Retour prévu", "Retard");

            for (Emprunt e : emprunts) {
                ajouterCellule(table, String.valueOf(e.getId()), Element.ALIGN_CENTER);
                ajouterCellule(table, e.getLivre().getTitre(), Element.ALIGN_LEFT);
                ajouterCellule(table, e.getUtilisateur().getNom(), Element.ALIGN_LEFT);
                ajouterCellule(table, e.getDateEmprunt().format(DATE_FMT), Element.ALIGN_CENTER);
                ajouterCellule(table, e.getDateRetourPrevue().format(DATE_FMT), Element.ALIGN_CENTER);

                long retard = e.joursDeRetard();
                PdfPCell cellRetard = new PdfPCell(new Phrase(
                        retard > 0 ? retard + " j" : "—",
                        new Font(Font.HELVETICA, 9, Font.BOLD,
                                retard > 0 ? DANGER : SUCCESS)));
                cellRetard.setHorizontalAlignment(Element.ALIGN_CENTER);
                cellRetard.setPadding(6);
                cellRetard.setBorderColor(GRAY_HEADER);
                table.addCell(cellRetard);
            }

            document.add(table);
            ajouterFooter(document);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF emprunts", e);
        }
    }

    // ==================== RAPPORT AMENDES ====================

    public byte[] genererRapportAmendes(List<Amende> amendes) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 40, 40, 60, 40);
            PdfWriter.getInstance(document, baos);
            document.open();

            double total = amendes.stream().mapToDouble(Amende::getMontant).sum();

            ajouterHeader(document, "💰 Amendes",
                    "Total : " + amendes.size() + " amende(s) | "
                    + String.format("%.2f €", total));

            PdfPTable table = new PdfPTable(new float[]{1, 3, 3, 2, 2, 2});
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);

            ajouterEntetes(table, "ID", "Utilisateur", "Livre",
                    "Montant", "Date", "Statut");

            for (Amende a : amendes) {
                ajouterCellule(table, String.valueOf(a.getId()), Element.ALIGN_CENTER);
                ajouterCellule(table, a.getUtilisateur().getNom(), Element.ALIGN_LEFT);
                ajouterCellule(table, a.getEmprunt().getLivre().getTitre(), Element.ALIGN_LEFT);

                PdfPCell cellMontant = new PdfPCell(new Phrase(
                        String.format("%.2f €", a.getMontant()),
                        new Font(Font.HELVETICA, 9, Font.BOLD, DANGER)));
                cellMontant.setHorizontalAlignment(Element.ALIGN_CENTER);
                cellMontant.setPadding(6);
                cellMontant.setBorderColor(GRAY_HEADER);
                table.addCell(cellMontant);

                ajouterCellule(table, a.getDateCreation().format(DATE_FMT), Element.ALIGN_CENTER);

                PdfPCell cellStatut = new PdfPCell(new Phrase(
                        a.getPayee() ? "Payée" : "Impayée",
                        new Font(Font.HELVETICA, 9, Font.BOLD,
                                a.getPayee() ? SUCCESS : DANGER)));
                cellStatut.setHorizontalAlignment(Element.ALIGN_CENTER);
                cellStatut.setPadding(6);
                cellStatut.setBorderColor(GRAY_HEADER);
                table.addCell(cellStatut);
            }

            document.add(table);
            ajouterFooter(document);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF amendes", e);
        }
    }

    // ==================== OUTILS ====================

    private void ajouterHeader(Document document, String titre, String sousTitre)
            throws DocumentException {
        // Logo / Titre
        Paragraph pTitre = new Paragraph(titre,
                new Font(Font.HELVETICA, 22, Font.BOLD, PRIMARY));
        pTitre.setAlignment(Element.ALIGN_LEFT);
        document.add(pTitre);

        // Sous-titre
        Paragraph pSousTitre = new Paragraph(sousTitre,
                new Font(Font.HELVETICA, 11, Font.NORMAL, GRAY_TEXT));
        pSousTitre.setSpacingBefore(4);
        document.add(pSousTitre);

        // Date du rapport
        Paragraph pDate = new Paragraph(
                "📅 Rapport généré le " + LocalDate.now().format(DATE_FMT),
                new Font(Font.HELVETICA, 9, Font.ITALIC, GRAY_TEXT));
        pDate.setSpacingBefore(4);
        document.add(pDate);

        // Ligne de séparation
        Paragraph ligne = new Paragraph(" ");
        ligne.setSpacingBefore(8);
        document.add(ligne);

        // Ligne horizontale
        PdfPTable separator = new PdfPTable(1);
        separator.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(PRIMARY);
        cell.setBorderWidth(2);
        cell.setFixedHeight(6);
        separator.addCell(cell);
        document.add(separator);
    }

    private void ajouterFooter(Document document) throws DocumentException {
        Paragraph espace = new Paragraph(" ");
        espace.setSpacingBefore(20);
        document.add(espace);

        Paragraph footer = new Paragraph(
                "📚 Bibliothèque Municipale — Rapport généré automatiquement",
                new Font(Font.HELVETICA, 8, Font.ITALIC, GRAY_TEXT));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    private void ajouterEntetes(PdfPTable table, String... entetes) {
        Font fontHeader = new Font(Font.HELVETICA, 9, Font.BOLD, PRIMARY);
        for (String e : entetes) {
            PdfPCell cell = new PdfPCell(new Phrase(e, fontHeader));
            cell.setBackgroundColor(GRAY_HEADER);
            cell.setPadding(8);
            cell.setBorderColor(GRAY_HEADER);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private void ajouterCellule(PdfPTable table, String texte, int alignement) {
        PdfPCell cell = new PdfPCell(new Phrase(texte,
                new Font(Font.HELVETICA, 9, Font.NORMAL)));
        cell.setPadding(6);
        cell.setBorderColor(GRAY_HEADER);
        cell.setHorizontalAlignment(alignement);
        table.addCell(cell);
    }
}