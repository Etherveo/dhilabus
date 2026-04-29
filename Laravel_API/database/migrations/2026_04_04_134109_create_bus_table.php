<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('bus', function (Blueprint $table) {
            $table->id('bus_id');
            $table->string('nama_bus');
            $table->string('plat_bus')->unique();
            $table->integer('kapasitas');
            $table->decimal('latitude', 10, 7)->nullable();
            $table->decimal('longitude', 11, 7)->nullable();
            $table->text('live_location_url')->nullable();
            $table->boolean('status')->default(false);
            $table->unsignedBigInteger('pakbus_user_id')->nullable();
            $table->foreign('pakbus_user_id')->references('user_id')->on('users')->nullOnDelete();
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('bus');
    }
};