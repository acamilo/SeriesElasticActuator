import com.neuronrobotics.bowlerstudio.creature.ICadGenerator;
import com.neuronrobotics.bowlerstudio.creature.CreatureLab;
import org.apache.commons.io.IOUtils;
import com.neuronrobotics.bowlerstudio.vitamins.*;
import eu.mihosoft.vrl.v3d.parametrics.*;
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;
import javafx.scene.paint.Color;
import eu.mihosoft.vrl.v3d.Transform;
import com.neuronrobotics.bowlerstudio.physics.TransformFactory;
import eu.mihosoft.vrl.v3d.Transform;

Vitamins.setGitRepoDatabase("https://github.com/madhephaestus/Hardware-Dimensions.git")

return new ICadGenerator(){
	HashMap<String , HashMap<String,ArrayList<CSG>>> map =  new HashMap<>();
	HashMap<String,ArrayList<CSG>> bodyMap =  new HashMap<>();
	LengthParameter thickness 		= new LengthParameter("Material Thickness",3.15,[10,1])
	LengthParameter printerOffset 		= new LengthParameter("printerOffset",0.5,[1.2,0])
	StringParameter boltSizeParam 			= new StringParameter("Bolt Size","M3",Vitamins.listVitaminSizes("capScrew"))
	StringParameter bearingSizeParam 			= new StringParameter("Encoder Board Bearing","608zz",Vitamins.listVitaminSizes("ballBearing"))

     String springType = "Torsion-9271K133"
     HashMap<String, Object>  springData = Vitamins.getConfiguration("torsionSpring",springType)
	HashMap<String, Object>  bearingData = Vitamins.getConfiguration("ballBearing",bearingSizeParam.getStrValue())			
	HashMap<String, Object>  boltMeasurments = Vitamins.getConfiguration( "capScrew",boltSizeParam.getStrValue())
	HashMap<String, Object>  nutMeasurments = Vitamins.getConfiguration( "nut",boltSizeParam.getStrValue())
	double gearDistance =30
	//println boltMeasurments.toString() +" and "+nutMeasurments.toString()
	double springHeight = springData.numOfCoils*springData.wireDiameter
	double linkMaterialThickness = 10 
	double boltDimeMeasurment = boltMeasurments.get("outerDiameter")
	double nutDimeMeasurment = nutMeasurments.get("width")
	double nutThickMeasurment = nutMeasurments.get("height")
	DHParameterKinematics neck=null;
	CSG bolt = Vitamins.get( "capScrew",boltSizeParam.getStrValue());
	CSG spring = Vitamins.get( "torsionSpring",springType)	
				.movez(-springHeight/2)
	CSG previousServo = null;
	CSG previousEncoder = null
	CSG encoder = (CSG) ScriptingEngine
					 .gitScriptRun(
            "https://github.com/madhephaestus/SeriesElasticActuator.git", // git location of the library
            "encoderBoard.groovy" , // file to load
            null// no parameters (see next tutorial)
            )
            .movez(-springHeight-linkMaterialThickness)
	/**
	 * Gets the all dh chains.
	 *
	 * @return the all dh chains
	 */
	public ArrayList<DHParameterKinematics> getLimbDHChains(MobileBase base) {
		ArrayList<DHParameterKinematics> copy = new ArrayList<DHParameterKinematics>();
		for(DHParameterKinematics l:base.getLegs()){
			copy.add(l);	
		}
		for(DHParameterKinematics l:base.getAppendages() ){
			copy.add(l);	
		}
		return copy;
	}
	
	@Override 
	public ArrayList<CSG> generateBody(MobileBase base ){
		ArrayList<CSG> attachmentParts = new ArrayList<CSG>()
		
		return attachmentParts;
	}
	@Override 
	public ArrayList<CSG> generateCad(DHParameterKinematics sourceLimb, int linkIndex) {
		
		String legStr = sourceLimb.getXml()
		LinkConfiguration conf = sourceLimb.getLinkConfiguration(linkIndex);

		String linkStr =conf.getXml()
		ArrayList<CSG> csg = null;
		HashMap<String,ArrayList<CSG>> legmap=null;
		if(map.get(legStr)==null){
			map.put(legStr, new HashMap<String,ArrayList<CSG>>())	
			// now load the cad and return it. 
		}
		legmap=map.get(legStr)
		if(legmap.get(linkStr) == null ){
			legmap.put(linkStr,new ArrayList<CSG>())
		}
		csg = legmap.get(linkStr)
		if(csg.size()>linkIndex){
			// this link is cached
			println "This link is cached"
			return csg;
		}
		//Creating the horn
		ArrayList<DHLink> dhLinks=sourceLimb.getChain().getLinks();
		DHLink dh = dhLinks.get(linkIndex);
		HashMap<String, Object> shaftmap = Vitamins.getConfiguration(conf.getShaftType(),conf.getShaftSize())
		double hornOffset = 	shaftmap.get("hornThickness")	
		
		// creating the servo
		CSG servoReference=   Vitamins.get(conf.getElectroMechanicalType(),conf.getElectroMechanicalSize())
		.transformed(new Transform().rotZ(90))
		
		double servoTop = servoReference.getMaxZ()
		CSG horn = Vitamins.get(conf.getShaftType(),conf.getShaftSize())	
		
		servoReference=servoReference
			.movez(-springHeight-linkMaterialThickness)			
			.movey(-gearDistance)
			.rotz(90+Math.toDegrees(dh.getTheta()))
		
		
		if(linkIndex==0){
			CSG baseServo =servoReference.clone()
			CSG secondLinkServo =servoReference.clone()
			CSG linkEncoder = encoder.clone()
						.rotz(-Math.toDegrees(dh.getTheta()))
			CSG baseEncoder = encoder.clone()
			
			previousEncoder = linkEncoder
			previousServo = secondLinkServo
			add(csg,baseServo,sourceLimb.getRootListener())
			add(csg,baseEncoder,sourceLimb.getRootListener())
			add(csg,secondLinkServo,dh.getListener())
			add(csg,linkEncoder,dh.getListener())
		}else{
			if(linkIndex<dhLinks.size()-1){
				CSG thirdPlusLinkServo =servoReference.clone()
				CSG linkEncoder = encoder.clone()
									.rotz(-Math.toDegrees(dh.getTheta()))

				previousEncoder = linkEncoder
				previousServo = thirdPlusLinkServo
				add(csg,thirdPlusLinkServo,dh.getListener())
				add(csg,linkEncoder,dh.getListener())
			}else{
				// load the end of limb
				// Target point
				CSG handMountPart = handMount()
				add(csg,handMountPart,dh.getListener())
				
			}
			
		}

		CSG forceSenseEncoder = moveDHValues(encoder
									.rotz(-Math.toDegrees(dh.getTheta()))
									.rotx(180)
									
		,dh)
		
		CSG springMoved = moveDHValues(spring
									.rotz(-Math.toDegrees(dh.getTheta()))
									.rotz(linkIndex==0?180:0)
									,dh)
		
		add(csg,forceSenseEncoder,dh.getListener())
		add(csg,springMoved,dh.getListener())
		
		
		return csg;
	}

	private CSG handMount(){
		
		CSG mountPlate = new Cube(5,30,70).toCSG()
		CSG centerHole =new Cylinder(10.2/2,10.2/2,10,(int)30)
							.toCSG()
							.movez(-5)
							.roty(90)
							
		
		mountPlate=mountPlate
					.toXMin()
					.difference(centerHole)
					.difference(bolt
								.roty(90)
								.toZMin()
								.movez(55.4/2)
								.toYMin()
								.movey(17.2/2)
					)
					.difference(bolt
								.roty(90)
								.toZMax()
								.movez(-55.4/2)
								.toYMin()
								.movey(17.2/2)
					)
					.difference(bolt
								.roty(90)
								.toZMax()
								.movez(-55.4/2)
								.toYMax()
								.movey(-17.2/2)
					)
					.difference(bolt
								.roty(90)
								.toZMin()
								.movez(55.4/2)
								.toYMax()
								.movey(-17.2/2)
					)
		// offset the claw mount so the tip is at the kinematic center
		mountPlate=mountPlate.movex(-54.4)
		return mountPlate
	}

	private CSG reverseDHValues(CSG incoming,DHLink dh ){
		println "Reversing "+dh
		TransformNR step = new TransformNR(dh.DhStep(0))
		Transform move = TransformFactory.nrToCSG(step)
		return incoming.transformed(move)
	}
	
	private CSG moveDHValues(CSG incoming,DHLink dh ){
		TransformNR step = new TransformNR(dh.DhStep(0)).inverse()
		Transform move = TransformFactory.nrToCSG(step)
		return incoming.transformed(move)
		
	}


	private add(ArrayList<CSG> csg ,CSG object, Affine dh ){
		object.setManipulator(dh);
		csg.add(object);
		BowlerStudioController.addCsg(object);
	}
};
