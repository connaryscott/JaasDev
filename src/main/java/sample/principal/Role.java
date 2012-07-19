package sample.principal;
import java.security.Principal;

public class Role implements Principal, java.io.Serializable {

        private String name;


        public Role(String name) {
            if (name == null)
                throw new NullPointerException("illegal null input");

            this.name = name;
        }

        public String getName() {
            return name;
        }


        public String toString() {
            return("Role:  " + name);
        }

        public boolean equals(Object o) {
            if (o == null)
                return false;

            if (this == o)
                return true;

            if (!(o instanceof Role))
                return false;
            Role that = (Role)o;

            if (this.getName().equals(that.getName()))
                return true;
            return false;
        }

        public int hashCode() {
            return name.hashCode();
        }
}
