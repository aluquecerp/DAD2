package vertx;

public class Temperatura {

	
	private int idTemp;
	private float valor;
	private long date;
	private int idDispositivo;
	
	
	


	public Temperatura() {
		this(0,0,0,0);
	}


	public Temperatura(int id, float valor, long date,int idDisp) {
		super();
		this.idTemp = id;
		this.valor = valor;
		this.date = date;
		this.idDispositivo = idDisp;

	}

	
	public int getIdTemp() {
		return idTemp;
	}


	public void setIdTemp(int idTemp) {
		this.idTemp = idTemp;
	}


	public int getIdDispositivo() {
		return idDispositivo;
	}


	public void setIdDispositivo(int idDispositivo) {
		this.idDispositivo = idDispositivo;
	}




	public float getValor() {
		return valor;
	}


	public void setValor(float valor) {
		this.valor = valor;
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
		result = prime * result + idDispositivo;
		result = prime * result + idTemp;
		result = prime * result + Float.floatToIntBits(valor);
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
		Temperatura other = (Temperatura) obj;
		if (date != other.date)
			return false;
		if (idDispositivo != other.idDispositivo)
			return false;
		if (idTemp != other.idTemp)
			return false;
		if (Float.floatToIntBits(valor) != Float.floatToIntBits(other.valor))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "Temperatura [idTemp=" + idTemp + ", valor=" + valor + ", date=" + date + ", idDispositivo="
				+ idDispositivo + "]";
	}



	
	
	
	
	
}
