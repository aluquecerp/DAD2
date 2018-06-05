package vertx;

public class Humedad {
	
		
		private int id;
		private long date;
		private float value;
		private int idDispositivo;
		
		public int getIdDispositivo() {
			return idDispositivo;
		}


		public void setIdDispositivo(int idDispositivo) {
			this.idDispositivo = idDispositivo;
		}


		public Humedad() {
			this(0,0,0,0);
		}
		
		public Humedad(int id, long date, float value, int idDispositivo) {
			super();
			this.id = id;
			this.date = date;
			this.value = value;
			this.idDispositivo = idDispositivo;
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


		public float getValue() {
			return value;
		}


		public void setValue(float value) {
			this.value = value;
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (date ^ (date >>> 32));
			result = prime * result + id;
			result = prime * result + idDispositivo;
			result = prime * result + Float.floatToIntBits(value);
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
			Humedad other = (Humedad) obj;
			if (date != other.date)
				return false;
			if (id != other.id)
				return false;
			if (idDispositivo != other.idDispositivo)
				return false;
			if (Float.floatToIntBits(value) != Float.floatToIntBits(other.value))
				return false;
			return true;
		}


		@Override
		public String toString() {
			return "Humedad [id=" + id + ", date=" + date + ", value=" + value + ", idDispositivo=" + idDispositivo
					+ "]";
		}


		
		
		
		
		
		
		
		
		
		
	


}
