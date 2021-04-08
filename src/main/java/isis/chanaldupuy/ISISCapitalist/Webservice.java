/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package isis.chanaldupuy.ISISCapitalist;

import isis.chanaldupuy.ISISCapitalist.generated.PallierType;
import isis.chanaldupuy.ISISCapitalist.generated.ProductType;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.springframework.web.bind.annotation.RequestBody;

/**
 *
 * @author Emma
 */
@Path("generic")
public class Webservice {

    Services services;

    public Webservice() {
        services = new Services();
    }

    @GET
    @Path("world")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getWorld(@Context HttpServletRequest request) {
        String username = request.getHeader("X-user");
        return Response.ok(services.getWorld(username)).build();
    }
    
    @PUT
    @Path("product")
    public void putProduct(@Context HttpServletRequest request, @RequestBody ProductType newprod){
        String username = request.getHeader("X-user");
        services.updateProduct(username, newprod);
    }
    
    @PUT
    @Path("manager")
    public void putManager(@Context HttpServletRequest request, @RequestBody PallierType newmana){
        String username = request.getHeader("X-user");
        services.updateManager(username, newmana);
    }
    
    /*@PUT
    @Path("upgarde")
    public void putUpgrade(@Context HttpServletRequest request){
        String username = request.getHeader("X-user");
    }*/
    
    
}
