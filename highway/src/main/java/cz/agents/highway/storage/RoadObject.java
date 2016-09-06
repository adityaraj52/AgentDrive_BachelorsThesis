package cz.agents.highway.storage;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import cz.agents.highway.agent.motionAgent;

public class RoadObject {

    private int id = -1;
    private double updateTime = -1;
    private Point3f position;
    private Vector3f velocity;
    private int lane = -1, actualLane =-1;
    private int  cs=0;
    private Point3f range[]=new Point3f[2];
    private Point3f dynamicReservedArea[]= new Point3f[2];
            
    public RoadObject(int id, double updateTime, int laneIndex, Point3f position, Vector3f velocity) {
        this.id = id;
        this.updateTime = updateTime;
        this.lane = laneIndex;
        this.position = position;
        this.velocity = velocity;
        //System.out.println("For id "+id+" cs val "+ new motionAgent(id).cs_val);
        /*Point3f p = new Point3f(0, 0, 0);
        try{
        if(range[0]==null || range[1]==null){
        dynamicReservedArea[0]=p;
        dynamicReservedArea[1]=p;
        range[0]=p;
        range[1]=p;
        cs = 0;
        }}
        catch(Exception e){
            System.out.println("Exception caught in road object "+id);
        }*/
    }
        
    /*public int get_cs(){
        return cs;
    }
    
    public void set_cs(int val){
        cs=val;
    }
    
    public void set_range(Point3f p1, Point3f p2){
        range[0]=p1;
        range[1]=p2;
    }
    
    public void setDynamicReservedArea(Point3f p1, Point3f p2){
        dynamicReservedArea[0]=p1;
        dynamicReservedArea[1]=p2;
    }
    
    public Point3f[] getDynamicReservedArea(){
        return dynamicReservedArea;
    }
    public Point3f[] get_range(){
        return range;
    }
    */
    public RoadObject(int id) {
       this.id = id;
    }

    public int getId() {
        return id;
    }

    public double getUpdateTime() {
        return updateTime;
    }

    public int getLaneIndex() {
        return lane;
    }
    
    public int getActualLaneIndex() {
        return actualLane;
    }
    
    public void setActualLaneIndex(int val) {
        actualLane = val;
    }

    public Point3f getPosition() {
        return position;
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    @Override
    public String toString() {
        return "RoadObject [id = " + id + ", updateTime=" + updateTime + ", lane=" + lane
                + ", pos=" + position + ", v=" + velocity + "]";
    }

}
