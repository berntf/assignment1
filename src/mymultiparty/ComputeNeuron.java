package mymultiparty;

import java.util.ArrayList;

public class ComputeNeuron extends Neuron{
	ArrayList<Double> weights;
	ArrayList<Neuron> connections;
	int id;
	double value;
	boolean Proccessing;
	boolean finishedProccessing;
	AcctiVationFunction activationFunction;
	public ComputeNeuron mate(ComputeNeuron mate){
		ComputeNeuron ret = new ComputeNeuron();
		return ret;
		
	}
	
	@Override
	public double getValue() throws NeuronalLoopException {	
		if (finishedProccessing)
			return activationFunction.Calculate(value);
		if(Proccessing){
			throw new NeuronalLoopException();
		}
		value=0;
		for (int i=0;i <weights.size();i++){
			try{
				value=value+weights.get(i)*connections.get(i).getValue();
			}catch (NeuronalLoopException e){
				weights.remove(i);
				connections.remove(i);
			}
		}
			finishedProccessing=true;
		return activationFunction.Calculate(value);
	}
	
}
