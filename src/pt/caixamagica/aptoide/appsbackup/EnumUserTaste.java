package pt.caixamagica.aptoide.appsbackup;

/**
 * @author rafael
 * @since 2.5.3
 * 
 */
public enum EnumUserTaste { 

	LIKE, DONTLIKE, TASTELESS, NOTEVALUATED; 
	
	@Override
	public String toString() {
		return super.toString().toLowerCase();
	}
	
}
