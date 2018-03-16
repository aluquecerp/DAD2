package vertx;

public class Toldo {

	
	private int id;
	private boolean state;
	private long date;
	
	
	public Toldo() {
		this(0,false,0);
	}


	public Toldo(int id, boolean state, long date) {
		super();
		this.id = id;
		this.state = state;
		this.date = date;

	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}



	public boolean isState() {
		return state;
	}


	public void setState(boolean state) {
		this.state = state;
	}


	public long getDate() {
		return date;
	}


	public void setDate(long date) {
		this.date = date;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (date ^ (date >>> 32));
		result = prime * result + id;
		result = prime * result + (state ? 1231 : 1237);
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Toldo other = (Toldo) obj;
		if (date != other.date)
			return false;
		if (id != other.id)
			return false;
		if (state != other.state)
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "Toldo [id=" + id + ", state=" + state + ", date=" + date + "]";
	}



	
	
	
	
	
}
