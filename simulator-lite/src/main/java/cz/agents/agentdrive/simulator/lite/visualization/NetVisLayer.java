/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.agents.agentdrive.simulator.lite.visualization;

import cz.agents.alite.configurator.Configurator;
import cz.agents.alite.vis.layer.AbstractLayer;
import cz.agents.alite.vis.layer.VisLayer;
import cz.agents.highway.environment.roadnet.Network;
import cz.agents.highway.environment.roadnet.XMLReader;
import cz.agents.highway.vis.NetLayer;
import java.awt.Graphics2D;

/**
 *
 * @author ondra
 */
public class NetVisLayer extends AbstractLayer{
    
    private final NetLayer  netLayer;
    
    public NetVisLayer(){
        XMLReader.getInstance().read(Configurator.getParamString("simulator.net.folder","nets/junction-big/"));
        netLayer = new NetLayer(Network.getInstance());
    }
    
    @Override
    public void paint(Graphics2D canvas) {
        netLayer.paint(canvas);
    }

    public static VisLayer create() {
        NetVisLayer layer = new NetVisLayer();
        return layer;
    }

}
