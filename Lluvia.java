package vertx;

public class Lluvia {
	
	private int id;
	private long date;
	private boolean state;
	private int idDispositivo;
	
	
	public int getIdDispositivo() {
		return idDispositivo;
	}


	public void setIdDispositivo(int idDispositivo) {
		this.idDispositivo = idDispositivo;
	}


	public Lluvia() {
		this(0,0,false,0);
	}
	
	
	public Lluvia(int id, long date, boolean state, int idDisp) {
		super();
		this.id = id;
		this.date = date;
		this.state = state;
		this.idDispositivo = idDisp;
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public long getDate() {
		return date;
	}


	public void setDate(long date) {
		this.date = date;
	}


	public boolean isState() {
		return state;
	}


	public void setState(boolean state) {
		this.state = state;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (date ^ (date >>> 32));
		result = prime * result + id;
		result = prime * result + idDispositivo;
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
		Lluvia other = (Lluvia) obj;
		if (date != other.date)
			return false;
		if (id != other.id)
			return false;
		if (idDispositivo != other.idDispositivo)
			return false;
		if (state != other.state)
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "Lluvia [id=" + id + ", date=" + date + ", state=" + state + ", idDispositivo=" + idDispositivo + "]";
	}

	
	
	
}
