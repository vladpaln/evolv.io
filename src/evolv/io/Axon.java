package evolv.io;
import java.util.Iterator;
import java.util.List;

class Axon implements Comparable<Axon> {
	final static double MUTABILITY_MUTABILITY = 0.7f;
	final static int mutatePower = 9;
	final static double MUTATE_MULTI= Math.pow(0.5f, mutatePower);

	final static double AXON_START_MUTABILITY = 0.0005f;
	final static double STARTING_AXON_VARIABILITY = 1.0f;

	double weight;
	double mutability;

	private static int maxX;
	private static int maxY;

	public final int startX;
	public final int startY;
	public final int endX;
	public final int endY;

	public Axon(){
		weight = (Math.random() * 2 - 1) * STARTING_AXON_VARIABILITY;
		mutability = AXON_START_MUTABILITY;
		this.startX = rangeX();
		this.startY = rangeY();
		this.endX = rangeX()+1;
		this.endY = rangeY();
	}

	public Axon(double w, double m,int startX,int startY,int endX,int endY) {
		weight = w;
		mutability = m;
		this.startX = (startX+maxX)%maxX;
		this.startY = (startY+maxY)%maxY;
		this.endX = (endX+maxX)%maxX;
		this.endY = (endY+maxY)%maxY;
	}

	//all parents are assumed to have a compareTo() == 0
	public Axon(List<Axon> parents){
		Iterator<Axon> iter = parents.iterator();
		Axon p=null;
		int count = 0;

		//averages weight and mutability of parents
		while(iter.hasNext()){
			p = iter.next();
			weight = p.weight;
			mutability = p.mutability;
			count++;
		}
		weight = weight/count;
		mutability = mutability/count;
		
		this.startX = p.startX;
		this.startY = p.startY;
		this.endX = p.endX;
		this.endY = p.endY;
	}

	public Axon mutateAxon() {
		double mutabilityMutate = Math.pow(0.5f, pmRan() * MUTABILITY_MUTABILITY);
		return new Axon(weight + r() * mutability / MUTATE_MULTI, 
				mutability * mutabilityMutate,
				startX+positionMutate(),
				startY+positionMutate(),
				endX+positionMutate(),
				endY+positionMutate());
	}

	public static void setMaxX(int x){
		maxX = x;
	}
	public static void setMaxY(int y){
		maxY = y;
	}

	private double r() {
		return Math.pow(pmRan(), mutatePower);
	}

	private double pmRan() {
		return Math.random() * 2 - 1;
	}

	private int rangeX(){
		return (int) (Math.random()*(maxX-1));
	}

	private int rangeY(){
		return (int) (Math.random()*maxY);
	}

	private int positionMutate(){
		return (int) (Math.random()*2.01-1.005);
	}

	//does not sort by weight
	public int compareTo(Axon axon){
		if(this.startX>axon.startX){
			return 1;
		}
		if(this.startX<axon.startX){
			return -1;
		}
		if(this.startY>axon.startY){
			return 1;
		}
		if(this.startY<axon.startY){
			return -1;
		}
		if(this.endX>axon.endX){
			return 1;
		}
		if(this.endX<axon.endX){
			return -1;
		}
		if(this.endY>axon.endY){
			return 1;
		}
		if(this.endY<axon.endY){
			return -1;
		}
		return 0;
	}
}