import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class FolderItJShell {
    public static void main(String[] args) throws Exception {
        String clientId = "fPKfRJTOEyxFrNNH";
        String clientSecret = "I8b5VmhC4Xdtfn-iqksE9r~HPu";
        
        HttpClient client = HttpClient.newHttpClient();
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://auth.folderit.com/oauth2/token"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret))
            .build();
            
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Auth Token Response: " + response.statusCode());
        String tokenRaw = response.body();
        String token = tokenRaw.split("\"access_token\":\"")[1].split("\"")[0];
        
        String accountUid = "mprUk0ZilV";
        String parentUid = "5blVc0PTxf";
        
        System.out.println("Creating folder under " + parentUid + "...");
        String payload = "{\"action\":\"create\", \"folderUid\":\"" + parentUid + "\", \"folderName\":\"TestVendorAsn\"}";
        
        HttpRequest createReq = HttpRequest.newBuilder()
            .uri(URI.create("https://api.folderit.com/v2/accounts/" + accountUid + "/folders"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + token)
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build();
            
        HttpResponse<String> createRes = client.send(createReq, HttpResponse.BodyHandlers.ofString());
        System.out.println("Create Folder Response: " + createRes.statusCode());
        System.out.println("Create Folder Body: " + createRes.body());
    }
}
