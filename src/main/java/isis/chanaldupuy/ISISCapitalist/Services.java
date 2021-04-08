/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package isis.chanaldupuy.ISISCapitalist;

import isis.chanaldupuy.ISISCapitalist.generated.PallierType;
import isis.chanaldupuy.ISISCapitalist.generated.ProductType;
import isis.chanaldupuy.ISISCapitalist.generated.TyperatioType;
import isis.chanaldupuy.ISISCapitalist.generated.World;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Emma
 */
public class Services {

    World world = new World();

    String path = "./src/main/resources";

    public World readWorldFromXml(String user) {
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(World.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            InputStream input = getClass().getClassLoader().getResourceAsStream(user + "_world.xml");
            if (input == null) {
                input = getClass().getClassLoader().getResourceAsStream("world.xml");
            }
            world = (World) jaxbUnmarshaller.unmarshal(input);
        } catch (Exception ex) {
            System.out.println("Erreur lecture du fichier:" + ex.getMessage());
            ex.printStackTrace();
        }
        return world;
    }

    public void saveWorldToXml(World world, String user) {
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(World.class);
            Marshaller march = jaxbContext.createMarshaller();
            OutputStream output = new FileOutputStream(path + "/" + user + "_world.xml");
            march.marshal(world, output);
        } catch (Exception ex) {
            System.out.println("Erreur écriture du fichier:" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    World getWorld(String user) {
        World world = this.readWorldFromXml(user);
        saveWorldToXml(world, user);
        return readWorldFromXml(user);
    }

    private ProductType findProductById(World world, int id) {
        for (ProductType product : world.getProducts().getProduct()) {
            if (product.getId() == id) {
                return product;
            }
        }
        return null;
    }

    private PallierType findManagerByName(World world, String name) {
        for (PallierType manager : world.getManagers().getPallier()) {
            if (manager.getName().equals(name)) {
                return manager;
            }
        }
        return null;
    }

    // prend en paramètre le pseudo du joueur et le produit
    // sur lequel une action a eu lieu (lancement manuel de production ou
    // achat d’une certaine quantité de produit)
    // renvoie false si l’action n’a pas pu être traitée
    public Boolean updateProduct(String username, ProductType newproduct) {
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        // trouver dans ce monde, le produit équivalent à celui passé
        // en paramètre
        ProductType product = findProductById(world, newproduct.getId());
        if (product == null) {
            return false;
        }

        // calculer la variation de quantité. Si elle est positive c'est
        // que le joueur a acheté une certaine quantité de ce produit
        // sinon c’est qu’il s’agit d’un lancement de production.
        int qtchange = newproduct.getQuantite() - product.getQuantite();
        if (qtchange > 0) {
            // soustraire de l'argent du joueur le cout de la quantité
            // achetée et mettre à jour la quantité de product
            double buy = product.getCout() * ((1 - Math.pow(product.getCroissance(), qtchange)) / (1 - product.getCroissance()));
            world.setMoney(world.getMoney() - buy);
            product.setQuantite(newproduct.getQuantite());
            product.setCout(newproduct.getCout());

            for (PallierType p : product.getPalliers().getPallier()) {

                if ((product.getQuantite() > p.getSeuil()) && (!p.isUnlocked())) {
                    p.setUnlocked(true);

                    if (p.getTyperatio() == TyperatioType.VITESSE) {

                        if (product.getTimeleft() > 0) {
                            product.setTimeleft(product.getTimeleft() / 2);
                        }

                        product.setVitesse((int) (product.getVitesse() / p.getRatio()));
                    }

                    if (p.getTyperatio() == TyperatioType.GAIN) {
                        product.setRevenu(product.getRevenu() * p.getRatio());
                    }
                }
            }
        } else {
            // initialiser product.timeleft à product.vitesse
            // pour lancer la production
            product.setTimeleft(product.getVitesse());
        }
        // sauvegarder les changements du monde
        saveWorldToXml(world, username);
        return true;
    }

    // prend en paramètre le pseudo du joueur et le manager acheté.
    // renvoie false si l’action n’a pas pu être traitée
    public Boolean updateManager(String username, PallierType newmanager) {

        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);

        // trouver dans ce monde, le manager équivalent à celui passé
        // en paramètre
        PallierType manager = findManagerByName(world, newmanager.getName());
        if (manager == null) {
            return false;
        }

        // débloquer ce manager
        manager.setUnlocked(true);

        // trouver le produit correspondant au manager
        ProductType product = findProductById(world, manager.getIdcible());
        if (product == null) {
            return false;
        }

        // débloquer le manager de ce produit
        product.setManagerUnlocked(true);

        // soustraire de l'argent du joueur le cout du manager
        world.setMoney(world.getMoney() - manager.getSeuil());

        // sauvegarder les changements au monde
        saveWorldToXml(world, username);
        return true;
    }

    public void updatescore(World world) {
        long tmps = System.currentTimeMillis() - world.getLastupdate();
        for (ProductType prod : world.getProducts().getProduct()) {
            if (!prod.isManagerUnlocked()) {
                if ((tmps > prod.getTimeleft()) && (prod.getTimeleft() != 0)) {
                    world.setMoney(world.getMoney() + prod.getQuantite() * prod.getRevenu());
                    world.setScore(world.getScore() + prod.getQuantite() * prod.getRevenu());
                } else {
                    long temps = prod.getTimeleft() - tmps;
                    if (temps < 0) {
                        prod.setTimeleft(0);
                    } else {
                        prod.setTimeleft(temps);
                    }
                }
            } else {
                //temps écoulé/temps production = quatite fabriqué pdt temps écoulé 
                double qtef = Math.floor(tmps / prod.getVitesse());
                long tps = prod.getTimeleft() - tmps;

                world.setMoney(world.getMoney() + prod.getQuantite() * prod.getRevenu() * qtef);
                world.setScore(world.getScore() + prod.getQuantite() * prod.getRevenu() * qtef);

                if (tps < 0) {
                    prod.setTimeleft(0);
                } else {
                    prod.setTimeleft(tps);
                }
            }
        }
        world.setLastupdate(System.currentTimeMillis());
    }

}
