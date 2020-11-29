class Ship {
	public static void main(String[] args) {
		// todo improve
		String url = "http://localhost:80";             // url where ship is located
		String shipName = "zod";                        // name (@p) of the ship
		String code = "lidlut-tabwed-pillex-ridrup";    // auth code for the ship (obtained by typing +code in the dojo)

		Urbit ship = new Urbit(url, shipName, code);
		ship.connect(); // must be called done before anything else

	}
}