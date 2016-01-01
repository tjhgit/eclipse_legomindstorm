package qlearn;

public class GridWorld implements RLWorld {

	// Actions.
	final int N = 0;
	final int E = 1;
	final int S = 2; 
	final int W = 3;

	// dimension: { x, y, actions }
	final int[] dimSize = { 10, 10, 4 };

	public int[] getDimension() {

		return dimSize;
	}

	public int[] getNextState( int[] state, int action ) {

		int[] newstate = new int[state.length] ;
		System.arraycopy( state, 0, newstate, 0, state.length); 

		// N-W corner in coordinates 0,0
		if( action == N )
			newstate[1]--;
		else if( action == E )
			newstate[0]++;
		else if( action == S )
			newstate[1]++;
		else if( action == W )
			newstate[0]--;
		return newstate;
	}    

	public boolean validAction( int[] state, int action ) {

		// West border
		if( state[0] == 0 && action == W )
			return false;
		// East border
		else if( state[0] == 9 && action == E )
			return false;
		// North border
		else if( state[1] == 0 && action == N )
			return false;
		// South border
		else if( state[1] == 9 && action == S )
			return false;
		else return true;
	}

	public boolean endState( int[] state ) {

		// Absorbing state in north-east corner.
		if( state[0] == 5 && state[1] == 5 ) {  
			return true;
		}
		else return false;
	}

	public double getReward( int[] state, int action ) {

		// Square in the west of the goal state. 
		if( state[0] == 4 && state[1] == 5 ) {
			if ( action == E )
				return 1;
			else return 0;
		}
		// Square in the south of the goal state. 
		if( state[0] == 6 && state[1] == 5 ) {
			if ( action == W )
				return 1;
			else return 0;
		}
		// Square in the south of the goal state. 
		if( state[0] == 5 && state[1] == 4 ) {
			if ( action == S )
				return 1;
			else return 0;
		}
		// Square in the south of the goal state. 
		if( state[0] == 5 && state[1] == 6 ) {
			if ( action == N )
				return 1;
			else return 0;
		}
		else return 0;
	}

	public void resetState( int[] state ) {
		// reposition to 0,0
		for( int j = 0 ; j < 2 ; j++ )
			state[j] = (int) ( Math.random() * dimSize[j] );
	}

	public double getInitValues() {
		return 0;
	}

	@Override
	public int[] resetState1(int[] state) {
		// TODO Auto-generated method stub
		return null;
	}



}
