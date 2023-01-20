package comptoirs.service;

import comptoirs.dao.CommandeRepository;
import comptoirs.dao.ProduitRepository;
import comptoirs.entity.Commande;
import comptoirs.entity.Ligne;
import comptoirs.entity.Produit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
 // Ce test est basé sur le jeu de données dans "test_data.sql"
class CommandeServiceTest {
    private static final String ID_PETIT_CLIENT = "0COM";
    private static final String ID_GROS_CLIENT = "2COM";
    private static final String VILLE_PETIT_CLIENT = "Berlin";
    private static final BigDecimal REMISE_POUR_GROS_CLIENT = new BigDecimal("0.15");

    @Autowired
    private CommandeService service;

    @Autowired
    ProduitRepository produitDao;

    @Autowired
    CommandeRepository commandeDao;

    @Test
    void testCreerCommandePourGrosClient() {
        var commande = service.creerCommande(ID_GROS_CLIENT);
        assertNotNull(commande.getNumero(), "On doit avoir la clé de la commande");
        assertEquals(REMISE_POUR_GROS_CLIENT, commande.getRemise(),
                "Une remise de 15% doit être appliquée pour les gros clients");
    }

    @Test
    void testCreerCommandePourPetitClient() {
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        assertNotNull(commande.getNumero());
        assertEquals(BigDecimal.ZERO, commande.getRemise(),
                "Aucune remise ne doit être appliquée pour les petits clients");
    }

    @Test
    void testCreerCommandeInitialiseAdresseLivraison() {
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        assertEquals(VILLE_PETIT_CLIENT, commande.getAdresseLivraison().getVille(),
                "On doit recopier l'adresse du client dans l'adresse de livraison");
    }

    @Test
    public void testEnregistreExpédition_orderDoesNotExist() {
        Integer commandeNum = 99;
        assertEquals(Optional.empty(), commandeDao.findById(commandeNum));
        assertThrows(IllegalArgumentException.class, () -> service.enregistreExpédition(commandeNum));
    }

    @Test
    public void testEnregistreExpédition_orderNotShipped() {
        Integer commandeNum = 1;
        LocalDate today = LocalDate.now();
        Commande commande = new Commande();
        commande.setEnvoyeele(null);
        assertEquals(Optional.of(commande), commandeDao.findById(commandeNum));
        Commande result = service.enregistreExpédition(commandeNum);
        assertNotNull(result.getEnvoyeele());
        assertEquals(today, result.getEnvoyeele());
    }

    @Test
    public void testEnregistreExpédition_orderAlreadyShipped() {
        // Arrange
        Integer commandeNum = 1;
        LocalDate shippedDate = LocalDate.of(2022, 1, 1);
        Commande commande = new Commande();
        commande.setEnvoyeele(shippedDate);
        assertEquals(Optional.of(commande), commandeDao.findById(commandeNum));
        Commande result = service.enregistreExpédition(commandeNum);
        assertEquals(shippedDate, result.getEnvoyeele());
    }

    @Test
    public void testEnregistreExpédition_decrementStock() {
        Integer commandeNum = 1;
        Commande commande = new Commande();
        commande.setEnvoyeele(null);
        Ligne ligne1 = new Ligne();
        Produit produit1 = new Produit();
        produit1.setUnitesEnStock(10);
        ligne1.setProduit(produit1);
        ligne1.setQuantite(3);
        Ligne ligne2 = new Ligne();
        Produit produit2 = new Produit();
        produit2.setUnitesEnStock(5);
        ligne2.setProduit(produit2);
        ligne2.setQuantite(2);
        commande.setLignes(Arrays.asList(ligne1, ligne2));
        assertEquals(Optional.of(commande), commandeDao.findById(commandeNum));
        Commande result = service.enregistreExpédition(commandeNum);
        assertEquals(7, produit1.getUnitesEnStock());
        assertEquals(3, produit2.getUnitesCommandees());
    }
}
