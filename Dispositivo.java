package vertx;

public class Dispositivo {
	
	private int idDispositivo;
	private String alias;
	private String dirIP;
	
	
	
	public Dispositivo() {
		this(0,"","");
	}
	public Dispositivo(int idDispositivo, String alias, String dirIP) {
		super();
		this.idDispositivo = idDispositivo;
		this.alias = alias;
		this.dirIP = dirIP;
	}
	public int getIdDispositivo() {
		return idDispositivo;
	}
	public void setIdDispositivo(int idDispositivo) {
		this.idDispositivo = idDispositivo;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getDirIP() {
		return dirIP;
	}
	public void setDirIP(String dirIP) {
		this.dirIP = dirIP;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alias == null) ? 0 : alias.hashCode());
		result = prime * result + ((dirIP == null) ? 0 : dirIP.hashCode());
		result = prime * result + idDispositivo;
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
		Dispositivo other = (Dispositivo) obj;
		if (alias == null) {
			if (other.alias != null)
				return false;
		} else if (!alias.equals(other.alias))
			return false;
		if (dirIP == null) {
			if (other.dirIP != null)
				return false;
		} else if (!dirIP.equals(other.dirIP))
			return false;
		if (idDispositivo != other.idDispositivo)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Dispositivo [idDispositivo=" + idDispositivo + ", alias=" + alias + ", dirIP=" + dirIP + "]";
	}
	
	
}
	
	