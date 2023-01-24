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

    static final int NUMERO_COMMANDE_PAS_LIVREE  = 99998;
    static final int NUMERO_COMMANDE_DEJA_LIVREE = 99999;

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
    }

    @Test
    public void testEnregistreExpédition_orderNotShipped() {
        Integer commandeNum = NUMERO_COMMANDE_PAS_LIVREE;
        Commande result = service.enregistreExpédition(commandeNum);
        assertNotNull(result.getEnvoyeele());
        assertEquals(LocalDate.now(), result.getEnvoyeele());
    }

    @Test
    void checkdateExpedition() {
        Integer commandeNumero = NUMERO_COMMANDE_PAS_LIVREE;
        var commande = service.enregistreExpédition(commandeNumero);
        assertEquals(LocalDate.now(), commande.getEnvoyeele(), "La commande est déjà envoyée!");
    }

    @Test
    void testEnregistreExpédition_decrementStock() {
        Integer commandeNumero = NUMERO_COMMANDE_PAS_LIVREE;
        var commande = service.enregistreExpédition(commandeNumero);
        for (Ligne ligne : commande.getLignes()) {
            assertEquals(10, ligne.getProduit().getUnitesEnStock());
        }
    }
}
