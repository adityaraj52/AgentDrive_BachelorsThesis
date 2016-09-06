/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.highway.agent;

import javax.vecmath.Point3f;

/**
 *
 * @author aditya
 */
public class add2RoadObject {
    private int critical_section=0, actuallane=-1;
    private Point3f range[] = new Point3f[2];

    public add2RoadObject(int val, Point3f range1 , Point3f range2, int lane) {
        critical_section=val;
        range[0]=range1;
        range[1]=range2;
        actuallane=lane;
    }
    public add2RoadObject(){
        
    }
    

    public int get_cs(){
        return critical_section;
    }
    
    public void set_cs(int val){
        critical_section=val;
    }
    
    public void set_range(Point3f p1, Point3f p2){
        range[0]=p1;
        range[1]=p2;
    }
    
    public Point3f[] get_range(){
        return range;
    }
    
    public void setActualLaneIndex(int val){
        actuallane=val;
    }
    
    public int getActualLaneIndex(){
        return actuallane;
    }
}
