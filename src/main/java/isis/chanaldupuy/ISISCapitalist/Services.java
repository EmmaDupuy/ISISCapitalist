/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package isis.chanaldupuy.ISISCapitalist;

import generated.World;
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
    public World readWorldFromXml() {
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(World.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
            //System.out.println(input);//vérifier ce que contient l'input
            /*if (input == null) {
                input = getClass().getClassLoader().getResourceAsStream("StarWars.xml");
            }*/
            world = (World) jaxbUnmarshaller.unmarshal(input);
        } catch (Exception ex) {
            System.out.println("Erreur lecture du fichier:" + ex.getMessage());
            ex.printStackTrace();
        }
        return world;
    }

    public void saveWorldToXml(World world, String username) {
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(World.class);
            Marshaller march = jaxbContext.createMarshaller();
            OutputStream output = new FileOutputStream(path + "/world.xml");
            march.marshal(world, output);
        } catch (Exception ex) {
            System.out.println("Erreur écriture du fichier:" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    World getWorld() {
        return readWorldFromXml();
    }
}
