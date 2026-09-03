using System;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Threading.Tasks;

class Program {
    static async Task Main() {
        var client = new HttpClient();
        var tokenReq = new HttpRequestMessage(HttpMethod.Post, "https://auth.folderit.com/oauth2/token");
        tokenReq.Content = new StringContent("grant_type=client_credentials&client_id=fPKfRJTOEyxFrNNH&client_secret=I8b5VmhC4Xdtfn-iqksE9r~HPu", System.Text.Encoding.UTF8, "application/x-www-form-urlencoded");
        var tokenRes = await client.SendAsync(tokenReq);
        var tokenStr = await tokenRes.Content.ReadAsStringAsync();
        var token = System.Text.Json.JsonDocument.Parse(tokenStr).RootElement.GetProperty("access_token").GetString();
        
        var req = new HttpRequestMessage(HttpMethod.Get, "https://api.folderit.com/v2/accounts/mprUk0ZilV/entities/all?folders=true&page=2");
        req.Headers.Authorization = new AuthenticationHeaderValue("Bearer", token);
        var res = await client.SendAsync(req);
        foreach(var h in res.Headers) {
            Console.WriteLine(h.Key + ": " + string.Join(", ", h.Value));
        }
    }
}
