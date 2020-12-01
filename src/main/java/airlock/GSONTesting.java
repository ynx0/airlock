import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class GSONTesting {

	public static void main(String[] args) {
		Map<String, Object> data = new HashMap<>();
		data.put("ship", "~sipfyn-pidmex");
		data.put("app", "hood");
		data.put("mark", "helm-hi");
		data.put("json", "Opening airlock");


		Map<String, Object> payload = new HashMap<>();
		payload.put("id", 1);
		payload.put("action", "poke");

		payload.putAll(data);


		Gson gson = new Gson();
		String json = gson.toJson(payload);
//		System.out.println("Resulting json:");
//		System.out.println(json);

		System.out.println(Urbit.uid());



	}

}
