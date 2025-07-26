import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import javax.imageio.*;
import java.awt.image.*;
public class Main{
	public static void main(String args[]){
		JFrame w = new JFrame();
		dP d = new dP();
		w.setTitle("3d test");
		w.setSize(500,500);
		w.setResizable(false);
		w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        w.add(d);
    	w.setVisible(true);
		javax.swing.Timer timer = new javax.swing.Timer(50,e -> d.repaint());
		timer.start();
	}
}
class dP extends JPanel{
	vec3 light_source1;
	vec3 cam;
	double camYaw;
	double camPitch;
	BufferedImage texture1;
	spawner sp;
	public void loadTextures(){
		
		try {
            texture1 = ImageIO.read(new File("dir.png"));
        } catch (IOException e) {
            System.err.println("Texture load failed.");
            e.printStackTrace();
        }
	}
	
	
	
	public dP() {
        	setDoubleBuffered(true);
			cam = new vec3(0,-3,0);
			light_source1 = new vec3(cam.x,cam.y,cam.z-2);
			camYaw = 0;
			camPitch = 0;
			loadTextures();
			sp = new spawner();
   	 }
	
	@Override
	protected void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0,0,getWidth(),getHeight());
		mesh tester = sp.test(0,0,5);
		AABB testHitbox = new AABB(new vec3(-5,-5,9.99),new vec3(5,5,10.99));
		drawMesh(tester,g2d,texture1);
	}
	public void drawMesh(mesh ts,Graphics2D g2d,BufferedImage texture){
		for (int i = 0; i < ts.tris.length; i++) {
			for (int j = 0; j < ts.tris[i].length; j++) {
				tri t = ts.tris[i][j];

				vec2 vn = t.v1.project(cam, camYaw, camPitch);
				vec2 v1n = t.v2.project(cam, camYaw, camPitch);
				vec2 v2n = t.v3.project(cam, camYaw, camPitch);
				int[] xPoints = { (int) vn.x, (int) v1n.x, (int) v2n.x };
				int[] yPoints = { (int) vn.y, (int) v1n.y, (int) v2n.y };
				Polygon triang = new Polygon(xPoints,yPoints,3);
				Rectangle bounds = triang.getBounds();
				g2d.setPaint(new TexturePaint(texture,bounds));
				g2d.fillPolygon(xPoints, yPoints, 3);
				if(t.v1.z < light_source1){
					float alpha = (t.v1.z - (light_source1)) * 0.5f; 
					g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
					g2d.setColor(new Color(255, 255, 255));
					g2d.fillPolygon(xPoints, yPoints, 3);
				}
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			}
		}



	}
}
class vec3{
	double x,y,z;
	double znear = 5;
	public vec3(double x,double y,double z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public vec2 project(vec3 cam,double yaw,double pitch){
		vec2 o = new vec2(0,0);
		double nX = this.x - cam.x;
		double nY = this.y - cam.y;
		double nZ = this.z - cam.z;
		if(nZ>=znear){
			double rotX = nX * Math.cos(yaw) - nZ * Math.sin(yaw);
			double rotZ = nX * Math.sin(yaw) + nZ * Math.cos(yaw);

			double finalY = nY * Math.cos(pitch) - rotZ * Math.sin(pitch);
			double finalZ = nY * Math.sin(pitch) + rotZ * Math.cos(pitch);
			
			double fov = 200; 
			double scale = fov / Math.max(finalZ, 0.1);
			double screenX = rotX * scale + 250;
			double screenY = finalY * scale + 250;
			o =  new vec2(screenX, screenY);
		}else if(nZ<znear){

			o =  new vec2(Double.NaN,Double.NaN);
		}
		return o;
	}
}
class vec2{
	double x,y;
	public vec2(double x,double y){
		this.x = x;
		this.y = y;
	}
}
class tri{
	vec3 v1,v2,v3;
	public tri(vec3 v1,vec3 v2,vec3 v3){
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
	}
}
class mesh{
	tri[][] tris;
	public mesh(tri[][] tris){
		this.tris = tris;
	}
}
class tester{
	double x,y,z;
	public tester(double x,double y,double z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	mesh m = new mesh(new tri[][] {
		{
			new tri(
				new vec3(x - 5, y - 5, z+5),
				new vec3(x - 5, y + 5, z+5),
				new vec3(x + 5, y + 5, z+5)
			)
		}
	});

}
class spawner{
	public spawner(){}
	public mesh test(double x,double y,double z){
		tester t = new tester(x,y,z);
		return t.m;
	}
}
class AABB{
    vec3 min,max;
    public AABB(vec3 min,vec3 max){
        this.min = min;
        this.max = max;
    }
    public boolean collidesWith(AABB other) {
    return (max.x > other.min.x && min.x < other.max.x) &&
           (max.y > other.min.y && min.y < other.max.y) &&
           (max.z > other.min.z && min.z < other.max.z);
	}

}
        
