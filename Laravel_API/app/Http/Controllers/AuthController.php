<?php

namespace App\Http\Controllers;

use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\RateLimiter;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Str;

class AuthController extends Controller
{
    private const MAX_LOGIN_ATTEMPTS = 5;
    private const LOGIN_DECAY_SECONDS = 60;

    public function register(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'name'        => 'required|string|max:255',
            'nim'         => 'required|string|max:15|unique:users,nim',
            'noTelp'      => 'required|string|max:20|unique:users,noTelp',
            'password'    => 'required|string|min:6',
            'device_name' => 'nullable|string|max:100',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validasi gagal',
                'errors'  => $validator->errors()
            ], 422);
        }

        $user = User::create([
            'name'     => $request->name,
            'nim'      => trim($request->nim),
            'noTelp'   => trim($request->noTelp),
            'password' => Hash::make($request->password),
            'role'     => 'mahasiswa',  // ← default mahasiswa
        ]);

        $token = $this->issueToken($user, $request);

        return response()->json([
            'success'      => true,
            'message'      => 'Registrasi berhasil',
            'role'         => $user->role,
            'data'         => $user,
            'access_token' => $token,
            'token_type'   => 'Bearer'
        ], 201);
    }

    // Login mahasiswa — pakai NIM
    public function login(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'nim'         => 'required|string|max:30',
            'password'    => 'required|string',
            'device_name' => 'nullable|string|max:100',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validasi gagal',
                'errors'  => $validator->errors()
            ], 422);
        }

        $nim         = trim($request->nim);
        $throttleKey = $this->throttleKey($nim, $request->ip());

        if (RateLimiter::tooManyAttempts($throttleKey, self::MAX_LOGIN_ATTEMPTS)) {
            $seconds = RateLimiter::availableIn($throttleKey);
            return response()->json([
                'success' => false,
                'message' => 'Terlalu banyak percobaan. Coba lagi dalam ' . $seconds . ' detik.'
            ], 429);
        }

        // ✅ Hanya cari mahasiswa — pakai nim saja
        $user = User::where('nim', $nim)
                    ->where('role', 'mahasiswa')
                    ->first();

        if (!$user || !Hash::check($request->password, $user->password)) {
            RateLimiter::hit($throttleKey, self::LOGIN_DECAY_SECONDS);
            return response()->json([
                'success' => false,
                'message' => 'NIM atau password salah'
            ], 401);
        }

        RateLimiter::clear($throttleKey);
        $user->tokens()->delete();
        $token = $this->issueToken($user, $request);

        return response()->json([
            'success'      => true,
            'message'      => 'Login berhasil',
            'role'         => $user->role,
            'data'         => $user,
            'access_token' => $token,
            'token_type'   => 'Bearer'
        ], 200);
    }

    // ✅ Login pakbus — pakai kode_pakbus
    public function loginPakbus(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'kode_pakbus' => 'required|string|max:20',
            'password'    => 'required|string',
            'device_name' => 'nullable|string|max:100',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validasi gagal',
                'errors'  => $validator->errors()
            ], 422);
        }

        $kode        = trim($request->kode_pakbus);
        $throttleKey = 'pakbus|' . $kode . '|' . ($request->ip() ?? 'unknown');

        if (RateLimiter::tooManyAttempts($throttleKey, self::MAX_LOGIN_ATTEMPTS)) {
            $seconds = RateLimiter::availableIn($throttleKey);
            return response()->json([
                'success' => false,
                'message' => 'Terlalu banyak percobaan. Coba lagi dalam ' . $seconds . ' detik.'
            ], 429);
        }

        $pakbus = User::where('kode_pakbus', $kode)
                      ->where('role', 'pakbus')
                      ->first();

        if (!$pakbus || !Hash::check($request->password, $pakbus->password)) {
            RateLimiter::hit($throttleKey, self::LOGIN_DECAY_SECONDS);
            return response()->json([
                'success' => false,
                'message' => 'Kode atau password salah'
            ], 401);
        }

        RateLimiter::clear($throttleKey);
        $pakbus->tokens()->delete();
        $token = $this->issueToken($pakbus, $request);

        return response()->json([
            'success'      => true,
            'message'      => 'Login berhasil',
            'role'         => $pakbus->role,
            'kode_pakbus'  => $pakbus->kode_pakbus,
            'data'         => $pakbus,
            'access_token' => $token,
            'token_type'   => 'Bearer'
        ], 200);
    }

    public function logout(Request $request)
    {
        if ($request->user()?->currentAccessToken()) {
            $request->user()->currentAccessToken()->delete();
        }
        return response()->json(['success' => true, 'message' => 'Logout berhasil']);
    }

    private function issueToken(User $user, Request $request): string
    {
        $deviceName = trim((string) $request->input('device_name', 'auth_token'));
        $tokenName  = $deviceName !== '' ? $deviceName : 'auth_token';
        return $user->createToken($tokenName)->plainTextToken;
    }

    private function throttleKey(string $nim, ?string $ip): string
    {
        return Str::lower($nim) . '|' . ($ip ?? 'unknown');
    }
}