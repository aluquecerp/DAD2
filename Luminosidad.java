package vertx;

public class Luminosidad {
	
		
		private int id;
		private long date;
		private float value;
		
		
		public Luminosidad() {
			this(0,0,0);
		}
		
		
		public Luminosidad(int id, long date, float value) {
			super();
			this.id = id;
			this.date = date;
			this.value = value;
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
			Luminosidad other = (Luminosidad) obj;
			if (date != other.date)
				return false;
			if (id != other.id)
				return false;
			if (Float.floatToIntBits(value) != Float.floatToIntBits(other.value))
				return false;
			return true;
		}


		@Override
		public String toString() {
			return "Luminosidad [id=" + id + ", date=" + date + ", value=" + value + "]";
		}
		
		
		
		
		
		
		
		
		
		
	


}
