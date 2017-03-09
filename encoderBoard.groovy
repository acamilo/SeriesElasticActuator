import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;
import eu.mihosoft.vrl.v3d.parametrics.*;

LengthParameter printerOffset 		= new LengthParameter("printerOffset",0.5,[1.2,0])
HashMap<String, Object>  bearingData = Vitamins.getConfiguration("ballBearing","608zz")

double magnetDiameter =6.0 - printerOffset.getMM()
double magnetThickness = 2.5
double magnetOffset =1.5
double mountHoleRadius = 2.0/2
double chipToShortside = 5.5
double chipToLongSide  = 9.0
double boardLong = 28
double boardShort = 22
double cornerOffset = (boardShort-(chipToLongSide*2))/2
double bearingOffset = magnetThickness+magnetOffset+1
double bearingHole =  bearingData.innerDiameter
double bearingHoleWithOffset =(bearingHole - printerOffset.getMM())/2
StringParameter bearingSizeParam 			= new StringParameter("Encoder Board Bearing","608zz",Vitamins.listVitaminSizes("ballBearing"))
CSG bearing = Vitamins.get("ballBearing",bearingSizeParam.getStrValue())
			.makeKeepaway(printerOffset.getMM())
			.movez(bearingOffset)

CSG bearingCutterSlot = new Cube(bearingHole,bearingHole,bearingOffset +bearingData.width).toCSG()
CSG bearingCutterSlotHole = new Cube(bearingHole*2/3,bearingHole,bearingOffset +bearingData.width).toCSG()
						.movez(1)
CSG bearingHoleInner = new Cylinder(bearingHoleWithOffset,bearingHoleWithOffset,bearingOffset +bearingData.width,(int)30).toCSG() // a one line Cylinder
CSG bearingHoleOuter = new Cylinder(bearingHole/2+2,bearingHole/2+2,bearingOffset +bearingData.width,(int)30).toCSG() // a one line Cylinder
					.difference(bearingHoleInner)

bearingCutterSlot= bearingCutterSlot
				.difference(bearingCutterSlotHole)
				.toZMin()
				.union(bearingHoleOuter)

CSG magnet =new Cylinder(magnetDiameter/2,magnetDiameter/2,magnetThickness+magnetOffset,(int)30).toCSG() // a one line Cylinder

CSG board = new Cube(	boardShort,// X dimention
			boardLong,// Y dimention
			1//  Z dimention
			).toCSG()
			.toZMax()
			.toYMin()
			.movey(-chipToShortside-cornerOffset)
CSG bolt =new Cylinder(mountHoleRadius,mountHoleRadius,10,(int)30).toCSG() // a one line Cylinder
						.movez(-5)

board=board.union(bolt
				.movex(chipToLongSide)
				.movey(chipToShortside)
				)
		.union(bolt
				.movex(-chipToLongSide)
				.movey(chipToShortside)
				)
		.union(bolt
				.movex(chipToLongSide)
				.movey(-chipToShortside)
				)
		.union(bolt
				.movex(-chipToLongSide)
				.movey(-chipToShortside)
				)
		.union(magnet)
		.union(bearing)
		.union(bearingCutterSlot)
return board